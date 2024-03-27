package `in`.procyk.shin.db

import `in`.procyk.shin.util.env
import io.github.cdimascio.dotenv.Dotenv
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object Database {
    fun init(dotenv: Dotenv) {
        Database.connect(
            url = dotenv.databaseUrl,
            user = dotenv.env("POSTGRES_USER"),
            password = dotenv.env("POSTGRES_PASSWORD"),
        )
        transaction {
            exec(CREATE_REDIRECT_TYPE)
            SchemaUtils.createMissingTablesAndColumns(ShortUrls)
        }
    }
}

private val Dotenv.databaseUrl: String
    get() {
        val host = env<String>("POSTGRES_HOST")
        val port = env<String>("POSTGRES_PORT")
        val db = env<String>("POSTGRES_DB")
        return "jdbc:postgresql://$host:$port/$db"
    }
