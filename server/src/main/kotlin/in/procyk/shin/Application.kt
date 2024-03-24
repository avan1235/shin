package `in`.procyk.shin

import `in`.procyk.shin.db.Database
import `in`.procyk.shin.service.singleShortUrlService
import `in`.procyk.shin.util.env
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.engine.*
import io.ktor.server.cio.*
import org.koin.dsl.module


fun main() {
    val dotenv = dotenv {
        ignoreIfMissing = true
        directory = "../"
    }
    Database.init(dotenv)
    val appModule = module {
        single<Dotenv> { dotenv }
        singleShortUrlService()
    }
    embeddedServer(
        factory = CIO,
        host = dotenv.env("HOST"),
        port = dotenv.env("PORT")
    ) {
        installPlugins(dotenv, appModule)
        installRoutes()
    }
        .start(wait = true)
}
