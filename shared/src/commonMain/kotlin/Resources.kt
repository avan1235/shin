import io.ktor.resources.*

@Resource("/shorten")
class Shorten(val url: String)

@Resource("/{shortenedId}")
class Decode(val shortenedId: String)
