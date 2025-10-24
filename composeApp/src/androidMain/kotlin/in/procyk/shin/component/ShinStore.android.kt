package `in`.procyk.shin.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.file.FileCodec
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
inline fun <reified T : @Serializable Any> rememberShinCodec(): Codec<T> {
    val context = LocalContext.current
    return remember {
        val filesDir = context.filesDir
        val file = filesDir.resolve(".shin")
        FileCodec<T>(
            file = Path(file.absolutePath),
            tempFile = Path(filesDir.resolve(Uuid.random().toHexDashString()).absolutePath)
        )
    }
}