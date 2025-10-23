package `in`.procyk.shin

import `in`.procyk.shin.service.ShortUrlService
import `in`.procyk.shin.shared.Decode
import `in`.procyk.shin.shared.RedirectType
import `in`.procyk.shin.shared.Shorten
import `in`.procyk.shin.shared.ShortenPath
import `in`.procyk.shin.util.env
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import io.ktor.server.resources.get as getResource
import io.ktor.server.routing.post as postBody

internal fun Application.installRoutes(): Routing = routing {
    val service by inject<ShortUrlService>()
    val dotenv by inject<Dotenv>()
    val redirectBaseUrl = dotenv.env<String>("REDIRECT_BASE_URL")
    postBody(ShortenPath) {
        val shorten = call.receive<Shorten>()
        handleShorten(service, redirectBaseUrl, shorten)
    }
    getResource<Decode> {
        handleDecode(service, it)
    }
}

private suspend inline fun RoutingContext.handleShorten(
    service: ShortUrlService,
    redirectBaseUrl: String,
    shorten: Shorten,
) {
    val shortId = service.findOrCreateShortenedId(shorten)
    if (shortId != null) {
        call.respond(HttpStatusCode.OK, "$redirectBaseUrl$shortId")
    } else {
        call.respond(HttpStatusCode.BadRequest)
    }
}

private suspend inline fun RoutingContext.handleDecode(
    service: ShortUrlService,
    decode: Decode,
) {
    val shortenedId = decode.shortenedId
    val shortened = service.findShortenedUrl(shortenedId)
    if (shortened != null) {
        call.respondRedirect(shortened.url, permanent = shortened.redirectType.isPermanent)
        service.increaseShortenedUrlUsageCount(shortenedId)
    } else {
        call.respond(HttpStatusCode.NotFound)
    }
}

private inline val RedirectType.isPermanent: Boolean
    get() = when (this) {
        RedirectType.MovedPermanently -> true
        RedirectType.Found -> false
    }
