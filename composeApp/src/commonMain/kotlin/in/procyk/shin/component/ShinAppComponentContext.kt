package `in`.procyk.shin.component

import androidx.compose.material3.SnackbarHostState
import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.storeOf

class ShinAppComponentContext private constructor(
    val store: KStore<ShinStore>,
) {
    val snackbarHostState: SnackbarHostState = SnackbarHostState()

    companion object {
        operator fun invoke(codec: Codec<ShinStore>): ShinAppComponentContext {
            val store = storeOf(codec, default = ShinStore.Default)
            return ShinAppComponentContext(store)
        }
    }
}
