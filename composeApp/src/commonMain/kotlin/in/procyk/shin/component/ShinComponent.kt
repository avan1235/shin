package `in`.procyk.shin.component

import Option
import Option.None
import Option.Some
import Shorten
import ShortenExpiring
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
import kotlinx.datetime.*
import kotlinx.datetime.Clock.System.now
import toLocalDate


interface ShinComponent : Component {

    val extraElementsVisible: Value<Boolean>

    val expirationDate: Value<LocalDate>

    val url: Value<String>

    val shortenedUrl: Value<Option<String>>

    val protocol: Value<ShortenedProtocol>

    fun onExtraElementsVisibleChange()

    fun onExpirationDateTimeChange(expirationDate: LocalDate?): Boolean

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

    private val _extraElementsVisible = MutableStateFlow(false)
    override val extraElementsVisible: Value<Boolean> = _extraElementsVisible.asValue()

    private val _expirationDate = MutableStateFlow(tomorrow)
    override val expirationDate: Value<LocalDate> = _expirationDate.asValue()

    private val _url = MutableStateFlow("")
    override val url: Value<String> = _url.asValue()

    private val _shortenedUrl = MutableStateFlow<Option<String>>(None)
    override val shortenedUrl: Value<Option<String>> = _shortenedUrl.asValue()

    private val _protocol = MutableStateFlow(ShortenedProtocol.HTTPS)
    override val protocol: Value<ShortenedProtocol> = _protocol.asValue()

    override fun onExtraElementsVisibleChange() {
        _extraElementsVisible.update { !it }
    }

    override fun onExpirationDateTimeChange(expirationDate: LocalDate?): Boolean = when {
        expirationDate == null -> {
            val updatedDate = tomorrow
            _expirationDate.update { updatedDate }
            true
        }

        expirationDate < now().toLocalDate() -> false

        else -> {
            _expirationDate.update { expirationDate }
            true
        }
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
        _shortenedUrl.update { None }
        _extraElementsVisible.update { false }
    }

    override fun onShorten() {
        scope.launch {
            httpClient.requestShortenedUrl(
                url = _url.value,
                shortenedProtocol = _protocol.value,
                expirationDate = _expirationDate.value.takeIfVisible(),
                onResponse = { response ->
                    val some = Some(response)
                    _shortenedUrl.update { some }
                },
                onError = { toast(it) }
            )
        }
    }

    private inline fun <T> T.takeIfVisible(): T? =
        takeIf { _extraElementsVisible.value }
}

private suspend fun HttpClient.requestShortenedUrl(
    url: String,
    shortenedProtocol: ShortenedProtocol,
    expirationDate: LocalDate?,
    onResponse: (String) -> Unit,
    onError: (String) -> Unit,
) {
    try {
        val expirationAt = expirationDate?.plus(1, DateTimeUnit.DAY)?.atStartOfDayIn(TimeZone.currentSystemDefault())
        val response = when (expirationAt) {
            null -> post<_>(Shorten(shortenedProtocol.buildUrl(url)))
            else -> post<_>(ShortenExpiring(shortenedProtocol.buildUrl(url), expirationAt))
        }
        response
            .takeIf { it.status == HttpStatusCode.OK }
            ?.bodyAsText()
            ?.let(onResponse)
    } catch (_: Exception) {
        onError("Cannot connect to Shin. Try again later…")
    }
}

private inline val tomorrow: LocalDate
    get() = now().toLocalDate().plus(1, DateTimeUnit.DAY)
