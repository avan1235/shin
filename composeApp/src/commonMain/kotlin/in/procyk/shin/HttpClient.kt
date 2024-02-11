package `in`.procyk.shin

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.*

internal fun createHttpClient(): HttpClient = HttpClient {
    install(Resources)
    defaultRequest {
        host = ComposeAppConfig.CLIENT_HOST
        url {
            protocol = URLProtocol.byName[ComposeAppConfig.CLIENT_PROTOCOL]!!
        }
    }
}