import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import `in`.procyk.shin.createHttpClient
import `in`.procyk.shin.ui.ShortenRequest
import `in`.procyk.shin.ui.ShortenResponse
import `in`.procyk.shin.ui.theme.ShinTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
