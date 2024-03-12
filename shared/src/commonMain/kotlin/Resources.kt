import io.ktor.resources.*
import kotlinx.datetime.Instant

@Resource("/shorten")
class Shorten(val url: String)

@Resource("/shorten")
class ShortenExpiring(val url: String, val expirationAt: Instant)

@Resource("/{shortenedId}")
class Decode(val shortenedId: String)
