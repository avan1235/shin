import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import `in`.procyk.shin.component.ShinComponent
import `in`.procyk.shin.shared.applyIf
import `in`.procyk.shin.shared.toNullable
import `in`.procyk.shin.ui.ShortenRequestItems
import `in`.procyk.shin.ui.ShortenResponse
import `in`.procyk.shin.ui.theme.ShinTheme
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import shin.composeapp.generated.resources.Mansalva_Regular
import shin.composeapp.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ShinApp(component: ShinComponent) {
    ShinTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(component.snackbarHostState) },
            modifier = Modifier.fillMaxSize(),
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
                        .onKeyEvent { event -> event.isEscDown.also { if (it) component.onShortenedUrlReset() } },
                    verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    item { }
                    item {
                        Text(
                            text = "Shin",
                            fontFamily = FontFamily(Font(Res.font.Mansalva_Regular)),
                            style = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.primary),
                            fontSize = 64.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            text = "Shorten Your URL with Kotlin",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    item {
                        val shortenedUrl by component.shortenedUrl.subscribeAsState()
                        ShortenResponse(shortenedUrl.toNullable())
                    }
                    ShortenRequestItems(
                        component = component,
                        maxWidth = maxWidth,
                        isVertical = isVertical,
                    )
                    item { }
                }
            }
        }
    }
}

private val KeyEvent.isEscDown: Boolean
    get() = key == Key.Escape && type == KeyEventType.KeyDown
