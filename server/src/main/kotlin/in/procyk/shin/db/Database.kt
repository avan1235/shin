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
            SchemaUtils.createMissingTablesAndColumns(ShortUrls)
        }
    }
}

private val Dotenv.databaseUrl: String
    get() = "jdbc:postgresql://${env<String>("POSTGRES_HOST")}:${env<String>("POSTGRES_PORT")}/${env<String>("POSTGRES_DB")}"
