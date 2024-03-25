package `in`.procyk.shin

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
    defaultRequest {
        host = ComposeAppConfig.CLIENT_HOST
        url {
            protocol = URLProtocol.byName[ComposeAppConfig.CLIENT_PROTOCOL]!!
        }
    }
}