package `in`.procyk.shin.component

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.storeOf

class ShinAppComponentContext private constructor(
    val store: KStore<ShinStore>,
) {
    val snackbarHostState: SnackbarHostState = SnackbarHostState()

    companion object {
        @Composable
        fun rememberShinAppComponentContext(codec: Codec<ShinStore>): ShinAppComponentContext {
            val store = remember(codec) { storeOf(codec, default = ShinStore.Default) }
            val context = remember(store) { ShinAppComponentContext(store) }
            return context
        }

        operator fun invoke(codec: Codec<ShinStore>): ShinAppComponentContext {
            val store = storeOf(codec, default = ShinStore.Default)
            val context = ShinAppComponentContext(store)
            return context
        }
    }
}
