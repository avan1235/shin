package `in`.procyk.shin.service

import `in`.procyk.shin.db.ShortUrl
import `in`.procyk.shin.db.ShortUrls
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
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
    suspend fun findOrCreateShortenedId(rawUrl: String, expirationAt: Instant?): String?

    suspend fun findShortenedUrl(shortenedId: String): String?

    suspend fun deleteExpiredUrls()
}

internal fun Module.singleShortUrlService() {
    single<ShortUrlService> { ShortUrlServiceImpl() }
}

private class ShortUrlServiceImpl : ShortUrlService {
    override suspend fun findOrCreateShortenedId(rawUrl: String, expirationAt: Instant?): String? {
        val url = rawUrl.normalizeAsUrl() ?: return null

        val id = url.sha256()
        return newSuspendedTransaction txn@{
            for (n in 1..id.length) {
                val shortId = id.take(n)
                val existing = ShortUrl.findById(shortId)
                when (existing?.url) {
                    null -> ShortUrl.new(shortId) {
                        this.url = url
                        this.expirationAt = expirationAt
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

    override suspend fun findShortenedUrl(shortenedId: String): String? = newSuspendedTransaction {
        ShortUrl.findById(shortenedId)?.url
    }

    override suspend fun deleteExpiredUrls() {
        val now = Clock.System.now()
        newSuspendedTransaction {
            ShortUrls.deleteWhere { (expirationAt.isNotNull()) and (expirationAt.lessEq(now)) }
        }
    }
}

fun String.normalizeAsUrl(): String? = try {
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
