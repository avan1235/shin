package `in`.procyk.shin

import ShinCbor
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.cbor.*
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
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