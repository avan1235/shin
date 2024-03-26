package `in`.procyk.shin.service

import `in`.procyk.shin.shared.RedirectType
import `in`.procyk.shin.shared.Shorten
import `in`.procyk.shin.db.ShortUrl
import `in`.procyk.shin.db.ShortUrls
import io.ktor.http.*
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.module.Module
import java.net.URI
import java.net.URISyntaxException
import java.security.MessageDigest
import java.util.*

internal interface ShortUrlService {
    suspend fun findOrCreateShortenedId(shorten: Shorten): String?

    suspend fun findShortenedUrl(shortenedId: String): ShortenedUrl?

    suspend fun deleteExpiredUrls()
}

internal fun Module.singleShortUrlService() {
    single<ShortUrlService> { ShortUrlServiceImpl() }
}

private class ShortUrlServiceImpl : ShortUrlService {
    override suspend fun findOrCreateShortenedId(shorten: Shorten): String? {
        val shortened = shorten.createShortenedIdentifier() ?: return null
        val expirationAt = shorten.expirationAt
        return newSuspendedTransaction txn@{
            for (count in shortened.takeCounts) {
                val shortId = shortened.uniqueId.take(count)
                val existing = ShortUrl.findById(shortId)
                when (existing?.url) {
                    null -> ShortUrl.new(shortId) {
                        this.url = shortened.url
                        this.expirationAt = expirationAt
                        this.redirectType = RedirectType.from(shorten.redirectType)
                    }.let { return@txn shortId }

                    shortened.url -> {
                        val prevExpirationAt = existing.expirationAt
                        when {
                            prevExpirationAt == null -> return@txn shortId

                            expirationAt == null || expirationAt > prevExpirationAt -> {
                                existing.expirationAt = expirationAt
                                return@txn shortId
                            }

                            else -> return@txn shortId
                        }
                    }

                    else -> continue
                }
            }
            null
        }
    }

    override suspend fun findShortenedUrl(shortenedId: String): ShortenedUrl? = newSuspendedTransaction {
        ShortUrl.findById(shortenedId)
    }?.let {
        ShortenedUrl(it.url, it.redirectType)
    }

    override suspend fun deleteExpiredUrls() {
        val now = Clock.System.now()
        newSuspendedTransaction {
            ShortUrls.deleteWhere { (expirationAt.isNotNull()) and (expirationAt.lessEq(now)) }
        }
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
} catch (e: URISyntaxException) {
    null
}

private fun String.sha256(): String = MessageDigest
    .getInstance("SHA-256")
    .digest(toByteArray())
    .let(Base64.getEncoder()::encodeToString)

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
            for (i in 1..id.length) yield((prefix?.let { it.length + 1 } ?: 0) + i)
        },
    )
}