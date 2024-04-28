package `in`.procyk.shin.ui.util

import androidx.compose.ui.input.key.*

internal val KeyEvent.isEscDown: Boolean
    get() = key == Key.Escape && type == KeyEventType.KeyDown
