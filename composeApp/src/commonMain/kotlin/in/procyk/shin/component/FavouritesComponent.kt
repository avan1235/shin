package `in`.procyk.shin.component

import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import `in`.procyk.shin.ui.util.createHttpClient
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds

interface FavouritesComponent : Component {

    val favourites: Value<List<Favourite>>

    fun onFavouriteClick(clipboardManager: ClipboardManager, shortUrl: String)

    fun overwriteFavourite(shortUrl: String)

    fun removeFavourite(shortUrl: String)

    fun isFavourite(shortUrl: String): Value<Boolean>
}

@Serializable
data class Favourite(
    val fullUrl: String,
    val shortUrl: String,
    val createdAt: Instant,
)

class FavouritesComponentImpl(
    appContext: ShinAppComponentContext,
    componentContext: ComponentContext,
) : AbstractComponent(appContext, componentContext), FavouritesComponent {

    private val httpClient: HttpClient = createHttpClient()

    private val settings = Settings()

    private val _favourites: Value<MutableMap<String, Favourite>> = callbackFlow {
        if (settings is ObservableSettings) {
            settings.addStringOrNullListener(FAVOURITES_KEY) {
                trySend(settings.loadFavourites())
            }
        } else launch {
            while (isActive) {
                delay(100.milliseconds)
                send(settings.loadFavourites())
            }
        }
        awaitClose()
    }
        .asValue(settings.loadFavourites())

    override val favourites: Value<List<Favourite>> =
        _favourites.map { it.map { it.value } }

    override fun onFavouriteClick(clipboardManager: ClipboardManager, shortUrl: String) {
        toast("Copied '$shortUrl' to clipboard")
        clipboardManager.setText(AnnotatedString(shortUrl))
    }

    override fun overwriteFavourite(shortUrl: String) {
        findResolvedUrl(shortUrl) { fullUrl ->
            val new = Favourite(fullUrl ?: return@findResolvedUrl, shortUrl, createdAt = Clock.System.now())
            val favourites = _favourites.value
            favourites[shortUrl] = new
            settings.saveFavourites(favourites)
        }
    }

    override fun removeFavourite(shortUrl: String) {
        val favourites = _favourites.value
        favourites.remove(shortUrl)
        settings.saveFavourites(favourites)
    }

    override fun isFavourite(shortUrl: String): Value<Boolean> {
        return _favourites.map { it[shortUrl] != null }
    }

    private inline fun findResolvedUrl(shortUrl: String, crossinline onResolved: (fullUrl: String?) -> Unit) {
        scope.launch {
            onResolved(httpClient.requestFullUrl(shortUrl))
        }
    }
}

private const val FAVOURITES_KEY: String = "FAVOURITES"

private val FavouritesJson = Json {
    ignoreUnknownKeys = true
}

private fun Settings.loadFavourites(): MutableMap<String, Favourite> {
    val favourites = getStringOrNull(FAVOURITES_KEY) ?: return mutableMapOf()
    return FavouritesJson.decodeFromString(favourites)
}

private fun Settings.saveFavourites(favourites: Map<String, Favourite>) {
    val encoded = FavouritesJson.encodeToString(favourites)
    putString(FAVOURITES_KEY, encoded)
}

private suspend fun HttpClient.requestFullUrl(shortUrl: String): String? {
    val response = get(shortUrl)
    if (response.status in REDIRECT_STATUS_CODES) {
        return response.call.response.headers[HttpHeaders.Location]
    }
    val index = shortUrl.indexOf("/#")
    if (index == -1) return null

    val productionShortUrl = shortUrl.replaceRange(0..index + 1, "https://api-shorten-kotlin.koyeb.app/")
    val productionResponse = get(productionShortUrl)
    if (productionResponse.status in REDIRECT_STATUS_CODES) {
        return productionResponse.call.response.headers[HttpHeaders.Location]
    }

    return null
}

private val REDIRECT_STATUS_CODES: Set<HttpStatusCode> = setOf(HttpStatusCode.MovedPermanently, HttpStatusCode.Found)
