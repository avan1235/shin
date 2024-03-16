package `in`.procyk.shin.db

import RedirectType
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.postgresql.util.PGobject

internal object ShortUrls : IdTable<String>() {
    override val id = varchar("id", 44).entityId()
    override val primaryKey = PrimaryKey(id)

    val url = text("url")
    val expirationAt = timestamp("expiration_at").nullable()
    val redirectType = customEnumeration(
        name = "redirect_type",
        sql = "redirect_type",
        fromDb = { value -> RedirectType.valueOf(value as String) },
        toDb = { PGEnum("redirect_type", it) },
    )
}

internal class ShortUrl(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, ShortUrl>(ShortUrls)

    var url by ShortUrls.url
    var expirationAt by ShortUrls.expirationAt
    var redirectType by ShortUrls.redirectType
}

val CREATE_REDIRECT_TYPE = """
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'redirect_type') THEN
        CREATE TYPE redirect_type AS ENUM
        (
        ${RedirectType.entries.joinToString { "'$it'" }}
        );
    END IF;
END$$;
"""

private class PGEnum<T : Enum<T>>(enumTypeName: String, enumValue: T?) : PGobject() {
    init {
        value = enumValue?.name
        type = enumTypeName
    }
}