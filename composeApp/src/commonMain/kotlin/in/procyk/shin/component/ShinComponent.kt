package `in`.procyk.shin.component

import Option
import Option.None
import Option.Some
import RedirectType
import SHORTEN_PATH
import ShinCbor
import Shorten
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import `in`.procyk.shin.createHttpClient
import `in`.procyk.shin.model.ShortenedProtocol
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.Clock.System.now
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import toLocalDate


interface ShinComponent : Component {

    val extraElementsVisible: Value<Boolean>

    val expirationDate: Value<LocalDate>

    val expirationDateVisible: Value<Boolean>

    val redirectType: Value<RedirectType>

    val redirectTypeVisible: Value<Boolean>

    val url: Value<String>

    val shortenedUrl: Value<Option<String>>

    val protocol: Value<ShortenedProtocol>

    fun onExtraElementsVisibleChange()

    fun onExpirationDateChange(expirationDate: LocalDate?): Boolean

    fun onExpirationDateVisibleChange(visible: Boolean)

    fun onRedirectTypeChange(redirectType: RedirectType)

    fun onRedirectTypeVisibleChange(visible: Boolean)

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

    private val _expirationDateVisible = MutableStateFlow(false)
    override val expirationDateVisible: Value<Boolean> = _expirationDateVisible.asValue()

    private val _redirectType = MutableStateFlow(RedirectType.Default)
    override val redirectType: Value<RedirectType> = _redirectType.asValue()

    private val _redirectTypeVisible = MutableStateFlow(false)
    override val redirectTypeVisible: Value<Boolean> = _redirectTypeVisible.asValue()

    private val _url = MutableStateFlow("")
    override val url: Value<String> = _url.asValue()

    private val _shortenedUrl = MutableStateFlow<Option<String>>(None)
    override val shortenedUrl: Value<Option<String>> = _shortenedUrl.asValue()

    private val _protocol = MutableStateFlow(ShortenedProtocol.HTTPS)
    override val protocol: Value<ShortenedProtocol> = _protocol.asValue()

    override fun onExtraElementsVisibleChange() {
        _extraElementsVisible.update { !it }
    }

    override fun onExpirationDateChange(expirationDate: LocalDate?): Boolean = when {
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

    override fun onExpirationDateVisibleChange(visible: Boolean) {
        _expirationDateVisible.update { visible }
    }

    override fun onRedirectTypeChange(redirectType: RedirectType) {
        _redirectType.update { redirectType }
    }

    override fun onRedirectTypeVisibleChange(visible: Boolean) {
        _redirectTypeVisible.update { visible }
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
                expirationDate = _expirationDate.value.takeIfExtraElementsVisibleAnd(expirationDateVisible),
                redirectType = _redirectType.value.takeIfExtraElementsVisibleAnd(redirectTypeVisible),
                onResponse = { response ->
                    val some = Some(response)
                    _shortenedUrl.update { some }
                },
                onError = { toast(it) }
            )
        }
    }

    private inline fun <T> T.takeIfExtraElementsVisibleAnd(value: Value<Boolean>): T? =
        takeIf { _extraElementsVisible.value && value.value }
}

@OptIn(ExperimentalSerializationApi::class)
private suspend fun HttpClient.requestShortenedUrl(
    url: String,
    shortenedProtocol: ShortenedProtocol,
    expirationDate: LocalDate?,
    redirectType: RedirectType?,
    onResponse: (String) -> Unit,
    onError: (String) -> Unit,
) {
    try {
        val expirationAt = expirationDate?.plus(1, DateTimeUnit.DAY)?.atStartOfDayIn(TimeZone.currentSystemDefault())
        val shorten = Shorten(shortenedProtocol.buildUrl(url), expirationAt, redirectType)
        val response = post(SHORTEN_PATH) {
            contentType(ContentType.Application.Cbor)
            setBody(ShinCbor.encodeToByteArray(shorten))
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
