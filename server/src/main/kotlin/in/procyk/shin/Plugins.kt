package `in`.procyk.shin

import `in`.procyk.shin.shared.ShinCbor
import `in`.procyk.shin.util.env
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.http.*
import io.ktor.serialization.kotlinx.cbor.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.resources.*
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.core.module.Module
import org.koin.ktor.plugin.Koin

@OptIn(ExperimentalSerializationApi::class)
internal fun Application.installPlugins(
    dotenv: Dotenv,
    appModule: Module,
) {
    install(Resources)
    install(ContentNegotiation) {
        cbor(ShinCbor)
    }
    installCors(dotenv)
    install(Koin) {
        modules(appModule)
    }
}

private fun Application.installCors(dotenv: Dotenv) {
    val corsPort = dotenv.env<String>("CORS_PORT")
    val corsHost = dotenv.env<String>("CORS_HOST")
    val corsScheme = dotenv.env<String>("CORS_SCHEME")
    install(CORS) {
        allowHost("${corsHost}:${corsPort}", schemes = listOf(corsScheme))
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.ContentLength)
        allowNonSimpleContentTypes = true
    }
}