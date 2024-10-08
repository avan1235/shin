package `in`.procyk.shin.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.Value
import `in`.procyk.shin.model.ShortenedProtocol
import `in`.procyk.shin.shared.*
import `in`.procyk.shin.shared.Option.None
import `in`.procyk.shin.shared.Option.Some
import `in`.procyk.shin.ui.util.createHttpClient
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.Clock.System.now
import kotlinx.serialization.encodeToByteArray
import toLocalDate

interface MainComponent : Component {

    val favourites: FavouritesComponent

    val extraElementsVisible: Value<Boolean>

    val customPrefix: Value<String>

    val customPrefixVisible: Value<Boolean>

    val oneTimeOnly: Value<Boolean>

    val expirationDate: Value<LocalDate>

    val expirationDateVisible: Value<Boolean>

    val redirectType: Value<RedirectType>

    val redirectTypeVisible: Value<Boolean>

    val fullUrl: Value<String>

    val shortenedUrl: Value<Option<String>>

    val protocol: Value<ShortenedProtocol>

    fun onExtraElementsVisibleChange()

    fun onCustomPrefixChange(customPrefix: String)

    fun onCustomPrefixVisibleChange(visible: Boolean)

    fun onOneTimeOnlyChange(oneTimeOnly: Boolean)

    fun onExpirationDateChange(expirationDate: LocalDate?): Boolean

    fun onExpirationDateVisibleChange(visible: Boolean)

    fun onRedirectTypeChange(redirectType: RedirectType)

    fun onRedirectTypeVisibleChange(visible: Boolean)

    fun onUrlChange(url: String)

    fun onProtocolChange(protocol: ShortenedProtocol)

    fun onShortenedUrlReset()

    fun onScanQRCode()

    fun onShorten()
}

class MainComponentImpl(
    appContext: ShinAppComponentContext,
    componentContext: ComponentContext,
    private val navigateOnScanQRCode: () -> Unit,
) : AbstractComponent(appContext, componentContext), MainComponent {

    private val httpClient: HttpClient = createHttpClient()

    override val favourites: FavouritesComponent =
        FavouritesComponentImpl(appContext, componentContext.childContext(key = "Favourites"))

    private val _extraElementsVisible = MutableStateFlow(false)
    override val extraElementsVisible: Value<Boolean> = _extraElementsVisible.asValue()

    private val _customPrefix = MutableStateFlow("")
    override val customPrefix: Value<String> = _customPrefix.asValue()

    private val _customPrefixVisible = MutableStateFlow(false)
    override val customPrefixVisible: Value<Boolean> = _customPrefixVisible.asValue()

    private val _oneTimeOnly = MutableStateFlow(false)
    override val oneTimeOnly: Value<Boolean> = _oneTimeOnly.asValue()

    private val _expirationDate = MutableStateFlow(tomorrow)
    override val expirationDate: Value<LocalDate> = _expirationDate.asValue()

    private val _expirationDateVisible = MutableStateFlow(false)
    override val expirationDateVisible: Value<Boolean> = _expirationDateVisible.asValue()

    private val _redirectType = MutableStateFlow(RedirectType.Default)
    override val redirectType: Value<RedirectType> = _redirectType.asValue()

    private val _redirectTypeVisible = MutableStateFlow(false)
    override val redirectTypeVisible: Value<Boolean> = _redirectTypeVisible.asValue()

    private val _fullUrl = MutableStateFlow("")
    override val fullUrl: Value<String> = _fullUrl.asValue()

    private val _shortenedUrl = MutableStateFlow<Option<String>>(None)
    override val shortenedUrl: Value<Option<String>> = _shortenedUrl.asValue()

    private val _protocol = MutableStateFlow(ShortenedProtocol.HTTPS)
    override val protocol: Value<ShortenedProtocol> = _protocol.asValue()

    override fun onExtraElementsVisibleChange() {
        _extraElementsVisible.update { !it }
    }

    override fun onCustomPrefixChange(customPrefix: String) {
        _customPrefix.update { customPrefix }
    }

    override fun onCustomPrefixVisibleChange(visible: Boolean) {
        _customPrefixVisible.update { visible }
    }

    override fun onOneTimeOnlyChange(oneTimeOnly: Boolean) {
        _oneTimeOnly.update { oneTimeOnly }
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
        _fullUrl.update { updatedUrl }
    }

    override fun onProtocolChange(protocol: ShortenedProtocol) {
        _protocol.update { protocol }
    }

    override fun onShortenedUrlReset() {
        _shortenedUrl.update { None }
        _extraElementsVisible.update { false }
    }

    override fun onScanQRCode() {
        navigateOnScanQRCode()
    }

    override fun onShorten() {
        scope.launch {
            httpClient.requestShortenedUrl(
                url = _fullUrl.value,
                shortenedProtocol = _protocol.value,
                customPrefix = _customPrefix.takeIfExtraElementsVisibleAnd(customPrefixVisible),
                oneTimeOnly = _oneTimeOnly.takeIfExtraElementsVisible(),
                expirationDate = _expirationDate.takeIfExtraElementsVisibleAnd(expirationDateVisible),
                redirectType = _redirectType.takeIfExtraElementsVisibleAnd(redirectTypeVisible),
                onResponse = { code, response ->
                    when (code) {
                        HttpStatusCode.OK -> {
                            val some = Some(response)
                            _shortenedUrl.update { some }
                        }

                        HttpStatusCode.BadRequest -> toast("Invalid URL")

                        else -> toast("Unknown error")
                    }
                },
                onError = { toast(it) }
            )
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun <T : Any> StateFlow<T>.takeIfExtraElementsVisibleAnd(value: Value<Boolean>): T? =
        this.value.takeIf { _extraElementsVisible.value && value.value }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun <T : Any> StateFlow<T>.takeIfExtraElementsVisible(): T? =
        this.value.takeIf { _extraElementsVisible.value }
}

private suspend inline fun HttpClient.requestShortenedUrl(
    url: String,
    shortenedProtocol: ShortenedProtocol,
    customPrefix: String?,
    oneTimeOnly: Boolean?,
    expirationDate: LocalDate?,
    redirectType: RedirectType?,
    onResponse: (HttpStatusCode, String) -> Unit,
    onError: (String) -> Unit,
) {
    try {
        val expirationAt = expirationDate?.plus(1, DateTimeUnit.DAY)
            ?.atStartOfDayIn(TimeZone.currentSystemDefault())
        val shorten = Shorten(shortenedProtocol.buildUrl(url), customPrefix, oneTimeOnly, expirationAt, redirectType)
        val response = post(ShortenPath) {
            contentType(ContentType.Application.Cbor)
            setBody(ShinCbor.encodeToByteArray(shorten))
        }
        val status = response.status
        val body = response.bodyAsText()
        onResponse(status, body)
    } catch (_: Exception) {
        onError("Cannot connect to Shin. Try again later…")
    }
}

private inline val tomorrow: LocalDate
    get() = now().toLocalDate().plus(1, DateTimeUnit.DAY)
