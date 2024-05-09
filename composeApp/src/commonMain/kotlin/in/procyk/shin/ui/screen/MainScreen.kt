package `in`.procyk.shin.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import `in`.procyk.shin.component.MainComponent
import `in`.procyk.shin.shared.applyIf
import `in`.procyk.shin.ui.component.*
import `in`.procyk.shin.ui.util.isEscDown

@Composable
internal fun MainScreen(
    component: MainComponent,
    isCameraAvailable: Boolean,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
    ) {
        val isVertical = maxHeight > maxWidth
        val maxWidth = maxWidth

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .applyIf(isVertical) { padding(horizontal = 16.dp) }
                .onKeyEvent { event ->
                    event.isEscDown.also { isConsumed -> if (isConsumed) component.onShortenedUrlReset() }
                },
            verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                ShinBanner()
            }
            item {
                ShortenResponse(component)
            }
            ShortenRequestItems(
                component = component,
                maxWidth = maxWidth,
                isVertical = isVertical,
                isCameraAvailable = isCameraAvailable,
            )
        }
    }
}
