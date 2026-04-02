package `in`.procyk.shin.component

import `in`.procyk.shin.model.ShortenedProtocol
import `in`.procyk.shin.shared.*
import `in`.procyk.shin.shared.Option.None
import `in`.procyk.shin.shared.Option.Some
import `in`.procyk.shin.ui.util.createHttpClient
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlin.time.Clock.System.now
import kotlinx.serialization.encodeToByteArray
import toLocalDate

interface MainComponent : Component {

    val extraElementsVisible: StateFlow<Boolean>

    val customPrefix: StateFlow<String>

    val customPrefixVisible: StateFlow<Boolean>

    val oneTimeOnly: StateFlow<Boolean>

    val expirationDate: StateFlow<LocalDate>

    val expirationDateVisible: StateFlow<Boolean>

    val redirectType: StateFlow<RedirectType>

    val redirectTypeVisible: StateFlow<Boolean>

    val fullUrl: StateFlow<String>

    val shortenedUrl: StateFlow<Option<String>>

    val protocol: StateFlow<ShortenedProtocol>

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
    scope: CoroutineScope,
    private val navigateOnScanQRCode: () -> Unit,
) : AbstractComponent(appContext, scope), MainComponent {

    private val httpClient: HttpClient = createHttpClient()

    override val extraElementsVisible: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override val customPrefix: MutableStateFlow<String> = MutableStateFlow("")

    override val customPrefixVisible: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override val oneTimeOnly: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override val expirationDate: MutableStateFlow<LocalDate> = MutableStateFlow(tomorrow)

    override val expirationDateVisible: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override val redirectType: MutableStateFlow<RedirectType> = MutableStateFlow(RedirectType.Default)

    override val redirectTypeVisible: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override val fullUrl: MutableStateFlow<String> = MutableStateFlow("")

    override val shortenedUrl: MutableStateFlow<Option<String>> = MutableStateFlow(None)

    override val protocol: MutableStateFlow<ShortenedProtocol> = MutableStateFlow(ShortenedProtocol.HTTPS)

    override fun onExtraElementsVisibleChange() {
        extraElementsVisible.update { !it }
    }

    override fun onCustomPrefixChange(customPrefix: String) {
        this.customPrefix.update { customPrefix }
    }

    override fun onCustomPrefixVisibleChange(visible: Boolean) {
        customPrefixVisible.update { visible }
    }

    override fun onOneTimeOnlyChange(oneTimeOnly: Boolean) {
        this.oneTimeOnly.update { oneTimeOnly }
    }

    override fun onExpirationDateChange(expirationDate: LocalDate?): Boolean = when {
        expirationDate == null -> {
            val updatedDate = tomorrow
            this.expirationDate.update { updatedDate }
            true
        }

        expirationDate < now().toLocalDate() -> false

        else -> {
            this.expirationDate.update { expirationDate }
            true
        }
    }

    override fun onExpirationDateVisibleChange(visible: Boolean) {
        expirationDateVisible.update { visible }
    }

    override fun onRedirectTypeChange(redirectType: RedirectType) {
        this.redirectType.update { redirectType }
    }

    override fun onRedirectTypeVisibleChange(visible: Boolean) {
        redirectTypeVisible.update { visible }
    }

    override fun onUrlChange(url: String) {
        val (updatedUrl, updatedProtocol) = ShortenedProtocol.simplifyInputUrl(url)
        updatedProtocol?.let { p -> protocol.update { p } }
        fullUrl.update { updatedUrl }
    }

    override fun onProtocolChange(protocol: ShortenedProtocol) {
        this.protocol.update { protocol }
    }

    override fun onShortenedUrlReset() {
        shortenedUrl.update { None }
        extraElementsVisible.update { false }
    }

    override fun onScanQRCode() {
        navigateOnScanQRCode()
    }

    override fun onShorten() {
        scope.launch {
            httpClient.requestShortenedUrl(
                url = fullUrl.value,
                shortenedProtocol = protocol.value,
                customPrefix = customPrefix.takeIfExtraElementsVisibleAnd(customPrefixVisible),
                oneTimeOnly = oneTimeOnly.takeIfExtraElementsVisible(),
                expirationDate = expirationDate.takeIfExtraElementsVisibleAnd(expirationDateVisible),
                redirectType = redirectType.takeIfExtraElementsVisibleAnd(redirectTypeVisible),
                onResponse = { code, response ->
                    when (code) {
                        HttpStatusCode.OK -> {
                            val some = Some(response)
                            shortenedUrl.update { some }
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
    private inline fun <T : Any> MutableStateFlow<T>.takeIfExtraElementsVisibleAnd(visible: StateFlow<Boolean>): T? =
        this.value.takeIf { extraElementsVisible.value && visible.value }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun <T : Any> MutableStateFlow<T>.takeIfExtraElementsVisible(): T? =
        this.value.takeIf { extraElementsVisible.value }
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
