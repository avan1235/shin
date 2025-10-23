package `in`.procyk.shin.service

import `in`.procyk.shin.db.ShortUrl
import `in`.procyk.shin.shared.RedirectType
import `in`.procyk.shin.shared.Shorten
import io.ktor.http.*
import kotlinx.datetime.toDeprecatedInstant
import kotlinx.datetime.toStdlibInstant
import kotlin.time.Clock
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.module.Module
import java.net.URI
import java.net.URISyntaxException
import java.security.MessageDigest
import java.util.*

internal interface ShortUrlService {
    suspend fun findOrCreateShortenedId(shorten: Shorten): String?

    suspend fun findShortenedUrl(shortenedId: String): ShortenedUrl?

    suspend fun increaseShortenedUrlUsageCount(shortenedId: String)
}

internal fun Module.singleShortUrlService() {
    single<ShortUrlService> { ShortUrlServiceImpl() }
}

private class ShortUrlServiceImpl : ShortUrlService {
    override suspend fun findOrCreateShortenedId(shorten: Shorten): String? {
        val shortened = shorten.createShortenedIdentifier() ?: return null
        val oneTimeOnly = shorten.oneTimeOnly
        val expirationAt = shorten.expirationAt
        return newSuspendedTransaction txn@{
            for (count in shortened.takeCounts) {
                val shortId = shortened.uniqueId.take(count)
                val existing = ShortUrl.findById(shortId)
                when (existing?.url) {
                    null -> ShortUrl.new(shortId) {
                        this.url = shortened.url
                        this.expirationAt = expirationAt?.toDeprecatedInstant()
                        this.oneTimeOnly = oneTimeOnly ?: false
                        this.active = true
                        this.redirectType = RedirectType.from(shorten.redirectType)
                        this.usageCount = 0
                    }.let { return@txn shortId }

                    shortened.url -> {
                        val prevExpirationAt = existing.expirationAt?.toStdlibInstant()
                        when {
                            prevExpirationAt == null -> {}

                            expirationAt == null || expirationAt > prevExpirationAt -> {
                                existing.expirationAt = expirationAt?.toDeprecatedInstant()
                            }

                            else -> {}
                        }

                        existing.active = true

                        val prevOneTimeOnly = existing.oneTimeOnly
                        if (prevOneTimeOnly && (oneTimeOnly == null || !oneTimeOnly)) {
                            existing.oneTimeOnly = false
                        }
                        return@txn shortId
                    }

                    else -> continue
                }
            }
            null
        }
    }

    override suspend fun findShortenedUrl(shortenedId: String): ShortenedUrl? = newSuspendedTransaction {
        val shortUrl = ShortUrl.findById(shortenedId)
        when {
            shortUrl == null || !shortUrl.active -> null
            shortUrl.oneTimeOnly -> shortUrl.also { it.active = false }
            shortUrl.expirationAt.let { exp -> exp != null && exp.toStdlibInstant() <= Clock.System.now() } ->
                null.also { shortUrl.active = false }

            else -> shortUrl
        }
    }?.let {
        ShortenedUrl(it.url, it.redirectType)
    }

    override suspend fun increaseShortenedUrlUsageCount(shortenedId: String) = newSuspendedTransaction txn@{
        val shortenedUrl = ShortUrl.findById(shortenedId) ?: return@txn
        println("increase for ${shortenedUrl.id}")
        shortenedUrl.usageCount += 1
    }
}

data class ShortenedUrl(
    val url: String,
    val redirectType: RedirectType,
)

private fun String.normalizeUrlCase(): String? = try {
    val uri = URI(this)
    val normalizedUri = URI(
        uri.scheme?.lowercase(),
        uri.userInfo,
        uri.host?.lowercase(),
        uri.port,
        uri.path,
        uri.query,
        uri.fragment,
    )
    normalizedUri.toString()
} catch (_: URISyntaxException) {
    null
}

private fun String.sha256(): String = MessageDigest
    .getInstance("SHA-256")
    .digest(toByteArray())
    .let(Base64.getUrlEncoder()::encodeToString)

private data class ShortenedIdentifier(
    val url: String,
    val uniqueId: String,
    val takeCounts: Sequence<Int>,
)

private fun Shorten.createShortenedIdentifier(): ShortenedIdentifier? {
    val url = url.normalizeUrlCase() ?: return null
    val prefix = customPrefix?.takeIf { it.isNotEmpty() }?.encodeURLPath(encodeSlash = true, encodeEncoded = false)
    val id = url.sha256().let { uniqueId ->
        prefix?.let { "$it-$uniqueId" } ?: uniqueId
    }
    return ShortenedIdentifier(
        url = url,
        uniqueId = id,
        takeCounts = sequence {
            prefix?.let { yield(it.length) }
            val shift = prefix?.let { it.length + 1 } ?: 0
            for (i in 1..id.length) yield(shift + i)
        },
    )
}
