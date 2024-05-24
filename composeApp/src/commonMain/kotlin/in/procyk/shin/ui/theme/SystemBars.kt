package `in`.procyk.shin.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import `in`.procyk.compose.util.SystemBarsScreen

@Composable
internal inline fun SystemBarsScreen(crossinline content: @Composable BoxScope.() -> Unit) {
    SystemBarsScreen(
        top = MaterialTheme.colorScheme.primaryContainer,
        bottom = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            content()
        }
    }
}
