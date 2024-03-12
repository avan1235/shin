package `in`.procyk.shin

import Decode
import Shorten
import ShortenExpiring
import `in`.procyk.shin.service.ShortUrlService
import `in`.procyk.shin.util.env
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.*
import kotlinx.datetime.Instant
import org.koin.ktor.ext.inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

@OptIn(DelicateCoroutinesApi::class)
internal fun Application.installRoutes(): Routing = routing {
    val service by inject<ShortUrlService>()
    val dotenv by inject<Dotenv>()
    val redirectBaseUrl = dotenv.env<String>("REDIRECT_BASE_URL")
    GlobalScope.launch {
        deleteExpiredUrlsEvery(1.hours, service)
    }
    post<Shorten> {
        handleShorten(service, redirectBaseUrl, it.url)
    }
    post<ShortenExpiring> {
        handleShorten(service, redirectBaseUrl, it.url, it.expirationAt)
    }
    get<Decode> {
        handleDecode(service, it.shortenedId)
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
    url: String,
    expirationAt: Instant? = null,
) {
    val shortId = service.findOrCreateShortenedId(url, expirationAt)
    if (shortId != null) call.respond(HttpStatusCode.OK, "$redirectBaseUrl$shortId")
    else call.respond(HttpStatusCode.BadRequest)
}

private suspend inline fun PipelineContext<Unit, ApplicationCall>.handleDecode(
    service: ShortUrlService,
    shortenedId: String,
) {
    val url = service.findShortenedUrl(shortenedId)
    if (url != null) call.respondRedirect(url, permanent = true)
    else call.respond(HttpStatusCode.NotFound)
}
