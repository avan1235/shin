package `in`.procyk.shin

import Decode
import Shorten
import `in`.procyk.shin.service.ShortUrlService
import `in`.procyk.shin.util.env
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

internal fun Application.installRoutes(): Routing = routing {
    val service by inject<ShortUrlService>()
    val dotenv by inject<Dotenv>()
    val redirectBaseUrl = dotenv.env<String>("REDIRECT_BASE_URL")
    post<Shorten> {
        val shortId = service.findOrCreateShortenedId(it.url)
        if (shortId != null) call.respond(HttpStatusCode.OK, "https://$redirectBaseUrl$shortId")
        else call.respond(HttpStatusCode.BadRequest)
    }
    get<Decode> {
        val shortenedId = it.shortenedId
        val url = service.findShortenedUrl(shortenedId)
        if (url != null) call.respondRedirect(url, permanent = true)
        else call.respond(HttpStatusCode.NotFound)
    }
}