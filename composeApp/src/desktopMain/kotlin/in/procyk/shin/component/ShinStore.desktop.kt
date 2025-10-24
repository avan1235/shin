package `in`.procyk.shin.component

import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.file.FileCodec
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.Serializable
import net.harawata.appdirs.AppDirsFactory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
inline fun <reified T : @Serializable Any> shinCodec(): Codec<T> {
    val filesDir = AppDirsFactory.getInstance().getUserDataDir(
        "in.procyk.shin",
        "local",
        "Maciej Procyk",
    )
    val file = Path(filesDir)

    with(SystemFileSystem) { if (!exists(file)) createDirectories(file) }

    return FileCodec<T>(Path(file, ".shin"), tempFile = Path(file, Uuid.random().toHexDashString()))
}