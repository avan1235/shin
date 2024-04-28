package `in`.procyk.shin.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds

interface FavouritesComponent : Component {

    val favourites: Value<List<Favourite>>

    fun overwriteFavourite(fullUrl: String, shortUrl: String)

    fun deleteFavourite(fullUrl: String)

    fun isFavourite(fullUrl: String, shortUrl: String): Value<Boolean>
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

    private val settings = Settings()

    private val _favourites = callbackFlow {
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

    override fun overwriteFavourite(fullUrl: String, shortUrl: String) {
        val favourites = _favourites.value
        val new = Favourite(fullUrl, shortUrl, createdAt = Clock.System.now())
        favourites[new.fullUrl] = new
        settings.saveFavourites(favourites)
    }

    override fun deleteFavourite(fullUrl: String) {
        val favourites = _favourites.value
        favourites.remove(fullUrl)
        settings.saveFavourites(favourites)
    }

    override fun isFavourite(fullUrl: String, shortUrl: String): Value<Boolean> {
        return _favourites.map { it[fullUrl]?.shortUrl == shortUrl }
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