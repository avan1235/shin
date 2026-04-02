package `in`.procyk.shin.component

import androidx.compose.runtime.Composable
import io.github.xxfast.kstore.Codec
import kotlinx.serialization.Serializable

typealias ShortUrl = String

@Composable
expect fun rememberShinCodec(): Codec<ShinStore>

@Serializable
data class ShinStore(
    val favorites: Map<ShortUrl, Favourite> = emptyMap(),
) {
    companion object {
        val Default: ShinStore = ShinStore()
    }
}
