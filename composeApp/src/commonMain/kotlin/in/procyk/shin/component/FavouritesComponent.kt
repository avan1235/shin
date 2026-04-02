package `in`.procyk.shin.component

import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import `in`.procyk.shin.ui.util.createHttpClient
import io.github.xxfast.kstore.KStore
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

interface FavouritesComponent : Component {

    val favourites: StateFlow<List<Favourite>>

    fun onFavouriteClick(clipboardManager: ClipboardManager, shortUrl: String)

    fun overwriteFavourite(shortUrl: String)

    fun removeFavourite(shortUrl: String)
}

@Serializable
data class Favourite(
    val fullUrl: String,
    val shortUrl: String,
    val createdAt: Instant,
)

class FavouritesComponentImpl(
    appContext: ShinAppComponentContext,
    scope: CoroutineScope,
) : AbstractComponent(appContext, scope), FavouritesComponent {

    private val httpClient: HttpClient = createHttpClient()

    private val store: KStore<ShinStore> = appContext.store

    override val favourites: StateFlow<List<Favourite>> =
        store.updates
            .map { it?.favorites?.values?.toList().orEmpty() }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override fun onFavouriteClick(clipboardManager: ClipboardManager, shortUrl: String) {
        toast("Copied '$shortUrl' to clipboard")
        clipboardManager.setText(AnnotatedString(shortUrl))
    }

    override fun overwriteFavourite(shortUrl: String) {
        findResolvedUrl(shortUrl) { fullUrl ->
            val new = Favourite(fullUrl ?: return@findResolvedUrl, shortUrl, createdAt = Clock.System.now())
            scope.launch {
                store.update { store ->
                    store?.copy(favorites = store.favorites + (shortUrl to new))
                }
            }
        }
    }

    override fun removeFavourite(shortUrl: String) {
        scope.launch {
            store.update { it?.copy(favorites = it.favorites - shortUrl) }
        }
    }

    private inline fun findResolvedUrl(shortUrl: String, crossinline onResolved: (fullUrl: String?) -> Unit) {
        scope.launch {
            onResolved(httpClient.requestFullUrl(shortUrl))
        }
    }
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
