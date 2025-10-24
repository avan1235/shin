package `in`.procyk.shin.component

import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.file.FileCodec
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalForeignApi::class, ExperimentalUuidApi::class)
inline fun <reified T : @Serializable Any> shinCodec(): Codec<T> {
    val fileManager = NSFileManager.defaultManager
    val documentsUrl = fileManager.URLForDirectory(
        directory = NSDocumentDirectory,
        appropriateForURL = null,
        create = false,
        inDomain = NSUserDomainMask,
        error = null
    )!!

    val documentsPath = documentsUrl.path!!
    return FileCodec<T>(
        file = Path(documentsPath, ".shin"),
        tempFile = Path(documentsPath, Uuid.random().toHexDashString())
    )
}