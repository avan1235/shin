package `in`.procyk.shin.component

import kotlinx.serialization.Serializable

typealias ShortUrl = String

@Serializable
data class ShinStore(
    val favorites: Map<ShortUrl, Favourite> = emptyMap(),
) {
    companion object {
        val Default: ShinStore = ShinStore()
    }
}
