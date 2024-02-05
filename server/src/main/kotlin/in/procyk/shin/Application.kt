package `in`.procyk.shin

import Decode
import Shorten
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import java.security.MessageDigest
import java.util.*

fun main() {
    val dotenv = dotenv {
        ignoreIfMissing = true
        directory = "../"
    }
    Database.connect(
        url = dotenv.databaseUrl,
        user = dotenv.env("POSTGRES_USER"),
        password = dotenv.env("POSTGRES_PASSWORD"),
    )
    transaction {
        SchemaUtils.createMissingTablesAndColumns(ShortUrls)
    }
    val appModule = module {
        single<Dotenv> { dotenv }
    }
    embeddedServer(
        factory = Netty,
        host = dotenv.env("HOST"),
        port = dotenv.env("PORT")
    ) {
        install(Resources)
        installCors(dotenv)
        install(Koin) {
            modules(appModule)
        }
        routes()
    }
        .start(wait = true)
}

private val Dotenv.databaseUrl: String
    get() = "jdbc:postgresql://${env<String>("POSTGRES_HOST")}:${env<String>("POSTGRES_PORT")}/${env<String>("POSTGRES_DB")}"

private fun Application.installCors(dotenv: Dotenv) {
    val corsPort = dotenv.env<String>("CORS_PORT")
    val corsHost = dotenv.env<String>("CORS_HOST")
    val corsScheme = dotenv.env<String>("CORS_SCHEME")
    install(CORS) {
        allowHost("${corsHost}:${corsPort}", schemes = listOf(corsScheme))
        allowMethod(HttpMethod.Post)
    }
}

private fun Application.routes(): Routing = routing {
    val dotenv by inject<Dotenv>()
    val redirectBaseUrl = dotenv.env<String>("REDIRECT_BASE_URL")
    post<Shorten> {
        val shortId = findShortenedId(it.url)
        if (shortId != null) call.respond(HttpStatusCode.OK, URLBuilder(redirectBaseUrl).apply { path(shortId) }.buildString())
        else call.respond(HttpStatusCode.InternalServerError)
    }
    get<Decode> {
        val shortenedId = it.shortenedId
        val url = newSuspendedTransaction {
            ShortUrl.findById(shortenedId)?.url
        }
        if (url != null) call.respondRedirect(url, permanent = true)
        else call.respond(HttpStatusCode.NotFound)
    }
}

private suspend fun findShortenedId(url: String): String? {
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

private object ShortUrls : IdTable<String>() {
    override val id = varchar("id", 44).entityId()
    override val primaryKey = PrimaryKey(id)

    val url = text("url")
}

internal class ShortUrl(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, ShortUrl>(ShortUrls)

    var url by ShortUrls.url
}

private fun String.sha256(): String = MessageDigest
    .getInstance("SHA-256")
    .digest(toByteArray())
    .let(Base64.getEncoder()::encodeToString)

private inline fun <reified T : Any> Dotenv.env(name: String): T {
    val value = get(name) ?: error("Environment variable $name is not defined in the system")
    return when (T::class) {
        String::class -> value as T
        Int::class -> value.toIntOrNull() as? T ?: error("$value cannot be converted to ${T::class.simpleName}")
        else -> throw IllegalArgumentException("Unsupported type ${T::class.simpleName}")
    }
}