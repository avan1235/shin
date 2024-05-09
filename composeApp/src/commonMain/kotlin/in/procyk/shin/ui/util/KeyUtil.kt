package `in`.procyk.shin.ui.util

import androidx.compose.ui.input.key.*

internal val KeyEvent.isEscDown: Boolean
    get() = this.key == Key.Escape && this.type == KeyEventType.KeyDown

internal fun KeyEvent.isKeyDown(key: Key): Boolean =
    this.key == key && this.type == KeyEventType.KeyDown
