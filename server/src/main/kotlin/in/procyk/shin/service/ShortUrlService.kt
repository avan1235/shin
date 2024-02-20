package `in`.procyk.shin.service

import `in`.procyk.shin.db.ShortUrl
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.module.Module
import java.net.URI
import java.net.URISyntaxException
import java.security.MessageDigest
import java.util.*

internal interface ShortUrlService {
    suspend fun findOrCreateShortenedId(rawUrl: String): String?

    suspend fun findShortenedUrl(shortenedId: String): String?
}

internal fun Module.singleShortUrlService() {
    single<ShortUrlService> { ShortUrlServiceImpl }
}

private object ShortUrlServiceImpl : ShortUrlService {
    override suspend fun findOrCreateShortenedId(rawUrl: String): String? {
        val url = rawUrl.normalizeAsUrl() ?: return null

        val id = url.sha256()
        return newSuspendedTransaction txn@{
            for (n in 1..id.length) {
                val shortId = id.take(n)
                val existing = ShortUrl.findById(shortId)
                when (existing?.url) {
                    null -> ShortUrl.new(shortId) { this.url = url }.let { return@txn shortId }
                    url -> return@txn shortId
                    else -> continue
                }
            }
            null
        }
    }

    override suspend fun findShortenedUrl(shortenedId: String): String? = newSuspendedTransaction {
        ShortUrl.findById(shortenedId)?.url
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
