package `in`.procyk.shin.ui.util

import `in`.procyk.shin.ComposeAppConfig
import `in`.procyk.shin.shared.ShinCbor
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.cbor.*

internal fun createHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) {
        cbor(ShinCbor)
    }
    followRedirects = false
    defaultRequest {
        host = ComposeAppConfig.CLIENT_HOST
        url {
            protocol = URLProtocol.byName[ComposeAppConfig.CLIENT_PROTOCOL]!!
        }
    }
}
