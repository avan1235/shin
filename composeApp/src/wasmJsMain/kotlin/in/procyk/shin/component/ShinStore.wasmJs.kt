package `in`.procyk.shin.component

import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.storage.StorageCodec
import kotlinx.serialization.Serializable

inline fun <reified T : @Serializable Any> shinCodec(): Codec<T> =
    StorageCodec(".shin")