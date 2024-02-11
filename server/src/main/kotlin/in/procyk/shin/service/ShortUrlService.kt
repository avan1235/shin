package `in`.procyk.shin.service

import `in`.procyk.shin.db.ShortUrl
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.module.Module
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL
import java.security.MessageDigest
import java.util.*

internal interface ShortUrlService {
    suspend fun findOrCreateShortenedId(url: String): String?

    suspend fun findShortenedUrl(shortenedId: String): String?
}

internal fun Module.singleShortUrlService() {
    single<ShortUrlService> { ShortUrlServiceImpl }
}

private object ShortUrlServiceImpl : ShortUrlService {
    override suspend fun findOrCreateShortenedId(url: String): String? {
        if (!url.isValidURL) return null

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

private inline val String.isValidURL: Boolean
    get() = try {
        URL(this).toURI()
        true
    } catch (e: MalformedURLException) {
        false
    } catch (e: URISyntaxException) {
        false
    }

private fun String.sha256(): String = MessageDigest
    .getInstance("SHA-256")
    .digest(toByteArray())
    .let(Base64.getEncoder()::encodeToString)
