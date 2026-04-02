package `in`.procyk.shin.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.storage.StorageCodec
import kotlinx.serialization.Serializable

@Composable
actual fun rememberShinCodec(): Codec<ShinStore> = remember { shinCodec() }

inline fun <reified T : @Serializable Any> shinCodec(): Codec<T> =
    StorageCodec(".shin")