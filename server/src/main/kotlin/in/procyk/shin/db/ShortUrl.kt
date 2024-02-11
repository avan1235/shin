package `in`.procyk.shin.db

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

internal object ShortUrls : IdTable<String>() {
    override val id = varchar("id", 44).entityId()
    override val primaryKey = PrimaryKey(id)

    val url = text("url")
}

internal class ShortUrl(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, ShortUrl>(ShortUrls)

    var url by ShortUrls.url
}