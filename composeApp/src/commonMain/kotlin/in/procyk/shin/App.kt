import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.procyk.shin.createHttpClient
import `in`.procyk.shin.ui.ShortenRequest
import `in`.procyk.shin.ui.ShortenResponse
import `in`.procyk.shin.ui.theme.ShinTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import shin.composeapp.generated.resources.Mansalva_Regular
import shin.composeapp.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ShinApp() {
    val client = remember { createHttpClient() }
    ShinTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        val snackbarHostStateScope = rememberCoroutineScope()
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) {
            var shortenedUrl by remember<MutableState<String?>> { mutableStateOf(null) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .onKeyEvent { event -> event.isEscDown.also { if (it) shortenedUrl = null } },
                verticalArrangement = Arrangement.Center,
            ) {
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
                Spacer(Modifier.size(32.dp))
                ShortenRequest(
                    client = client,
                    onResponse = { shortenedUrl = it },
                    onError = { snackbarHostStateScope.showErrorSnackbarNotification(snackbarHostState, it) })
                ShortenResponse(shortenedUrl)
            }
        }
    }
}

private fun CoroutineScope.showErrorSnackbarNotification(
    snackbarHostState: SnackbarHostState,
    message: String,
) {
    launch {
        snackbarHostState.showSnackbar(
            message = message,
            withDismissAction = true,
            duration = SnackbarDuration.Short,
        )
    }
}

private val KeyEvent.isEscDown: Boolean
    get() = key == Key.Escape && type == KeyEventType.KeyDown
