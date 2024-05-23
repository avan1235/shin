package `in`.procyk.shin.ui.util

import androidx.compose.ui.Modifier

inline fun Modifier.applyIf(condition: Boolean, f: Modifier.() -> Modifier): Modifier =
    if (condition) this.f() else this