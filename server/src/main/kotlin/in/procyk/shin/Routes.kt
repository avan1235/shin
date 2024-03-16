package `in`.procyk.shin

import Decode
import RedirectType
import SHORTEN_PATH
import Shorten
import `in`.procyk.shin.service.ShortUrlService
import `in`.procyk.shin.util.env
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.*
import org.koin.ktor.ext.inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import io.ktor.server.resources.get as getResource
import io.ktor.server.routing.post as postBody

@OptIn(DelicateCoroutinesApi::class)
internal fun Application.installRoutes(): Routing = routing {
    val service by inject<ShortUrlService>()
    val dotenv by inject<Dotenv>()
    val redirectBaseUrl = dotenv.env<String>("REDIRECT_BASE_URL")
    GlobalScope.launch {
        deleteExpiredUrlsEvery(1.hours, service)
    }
    postBody(SHORTEN_PATH) {
        val shorten = call.receive<Shorten>()
        handleShorten(service, redirectBaseUrl, shorten)
    }
    getResource<Decode> {
        handleDecode(service, it)
    }
}

private suspend inline fun deleteExpiredUrlsEvery(
    duration: Duration,
    service: ShortUrlService,
) {
    coroutineScope {
        while (isActive) {
            service.deleteExpiredUrls()
            delay(duration)
        }
    }
}

private suspend inline fun PipelineContext<Unit, ApplicationCall>.handleShorten(
    service: ShortUrlService,
    redirectBaseUrl: String,
    shorten: Shorten,
) {
    val shortId = service.findOrCreateShortenedId(shorten)
    if (shortId != null) call.respond(HttpStatusCode.OK, "$redirectBaseUrl$shortId")
    else call.respond(HttpStatusCode.BadRequest)
}

private suspend inline fun PipelineContext<Unit, ApplicationCall>.handleDecode(
    service: ShortUrlService,
    decode: Decode,
) {
    val shortened = service.findShortenedUrl(decode.shortenedId)
    if (shortened != null) call.respondRedirect(shortened.url, permanent = shortened.redirectType.isPermanent)
    else call.respond(HttpStatusCode.NotFound)
}

private inline val RedirectType.isPermanent: Boolean
    get() = when (this) {
        RedirectType.MovedPermanently -> true
        RedirectType.Found -> false
    }
