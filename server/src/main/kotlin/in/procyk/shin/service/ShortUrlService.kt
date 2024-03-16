package `in`.procyk.shin.service

import RedirectType
import Shorten
import `in`.procyk.shin.db.ShortUrl
import `in`.procyk.shin.db.ShortUrls
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
        val url = shorten.url.normalizeUrlCase() ?: return null

        val id = url.sha256()
        val expirationAt = shorten.expirationAt
        return newSuspendedTransaction txn@{
            for (n in 1..id.length) {
                val shortId = id.take(n)
                val existing = ShortUrl.findById(shortId)
                when (existing?.url) {
                    null -> ShortUrl.new(shortId) {
                        this.url = url
                        this.expirationAt = expirationAt
                        this.redirectType = RedirectType.from(shorten.redirectType)
                    }.let { return@txn shortId }

                    url -> {
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
