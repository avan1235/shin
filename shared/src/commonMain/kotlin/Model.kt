import io.ktor.resources.*
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor

const val SHORTEN_PATH: String = "/shorten"

@Serializable
enum class RedirectType {
    MovedPermanently,
    Found,
    ;

    companion object {
        val Default: RedirectType = MovedPermanently

        fun from(redirectType: RedirectType?): RedirectType = redirectType ?: Default
    }
}

@Serializable
class Shorten(
    val url: String,
    val customPrefix: String? = null,
    val expirationAt: Instant? = null,
    val redirectType: RedirectType? = null,
)

@Resource("/{shortenedId}")
class Decode(val shortenedId: String)

@OptIn(ExperimentalSerializationApi::class)
val ShinCbor = Cbor {
    encodeDefaults = true
    ignoreUnknownKeys = true
}
