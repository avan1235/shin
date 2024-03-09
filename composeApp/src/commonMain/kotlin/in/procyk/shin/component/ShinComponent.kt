package `in`.procyk.shin.component

import Option
import Option.Some
import Shorten
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import `in`.procyk.shin.createHttpClient
import `in`.procyk.shin.model.ShortenedProtocol
import io.ktor.client.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

interface ShinComponent : Component {

    val expirationDateTime: Value<Option<Instant>>

    val url: Value<String>

    val shortenedUrl: Value<Option<String>>

    val protocol: Value<ShortenedProtocol>

    fun onExpirationDateTimeChange(expirationDateTime: Instant?)

    fun onUrlChange(url: String)

    fun onProtocolChange(protocol: ShortenedProtocol)

    fun onShortenedUrlReset()

    fun onShorten()
}

class ShinComponentImpl(
    appContext: ShinAppComponentContext,
    componentContext: ComponentContext,
) : AbstractComponent(appContext, componentContext), ShinComponent {

    private val httpClient: HttpClient = createHttpClient()

    private val _expirationDateTime = MutableStateFlow<Option<Instant>>(Option.None)
    override val expirationDateTime: Value<Option<Instant>> = _expirationDateTime.asValue()

    private val _url = MutableStateFlow("")
    override val url: Value<String> = _url.asValue()

    private val _shortenedUrl = MutableStateFlow<Option<String>>(Option.None)
    override val shortenedUrl: Value<Option<String>> = _shortenedUrl.asValue()

    private val _protocol = MutableStateFlow(ShortenedProtocol.HTTPS)
    override val protocol: Value<ShortenedProtocol> = _protocol.asValue()

    override fun onExpirationDateTimeChange(expirationDateTime: Instant?) {
        val updatedValue = Option.fromNullable(expirationDateTime)
        _expirationDateTime.update { updatedValue }
    }

    override fun onUrlChange(url: String) {
        val (updatedUrl, updatedProtocol) = ShortenedProtocol.simplifyInputUrl(url)
        updatedProtocol?.let { protocol -> _protocol.update { protocol } }
        _url.update { updatedUrl }
    }

    override fun onProtocolChange(protocol: ShortenedProtocol) {
        _protocol.update { protocol }
    }

    override fun onShortenedUrlReset() {
        _shortenedUrl.update { Option.None }
    }

    override fun onShorten() {
        scope.launch {
            httpClient.requestShortenedUrl(
                url = _url.value,
                shortenedProtocol = _protocol.value,
                onResponse = { response ->
                    val some = Some(response)
                    _shortenedUrl.update { some }
                },
                onError = { toast(it) }
            )
        }
    }
}

private suspend fun HttpClient.requestShortenedUrl(
    url: String,
    shortenedProtocol: ShortenedProtocol,
    onResponse: (String) -> Unit,
    onError: (String) -> Unit,
) {
    try {
        post<Shorten>(Shorten(shortenedProtocol.buildUrl(url)))
            .takeIf { it.status == HttpStatusCode.OK }
            ?.bodyAsText()
            ?.let(onResponse)
    } catch (_: Exception) {
        onError("Cannot connect to Shin. Try again later…")
    }
}
