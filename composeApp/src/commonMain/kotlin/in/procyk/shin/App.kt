import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import `in`.procyk.shin.ui.ShortenRequest
import `in`.procyk.shin.ui.ShortenResponse
import `in`.procyk.shin.createHttpClient

@Composable
fun ShinApp() {
    val client = remember { createHttpClient() }
    MaterialTheme {
        var shortenedUrl by remember<MutableState<String?>> { mutableStateOf(null) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .onKeyEvent { event -> event.isEscDown.also { if (it) shortenedUrl = null } },
            verticalArrangement = Arrangement.Center,
        ) {
            ShortenRequest(client, onResponse = { shortenedUrl = it })
            ShortenResponse(shortenedUrl)
        }
    }
}

private val KeyEvent.isEscDown: Boolean
    get() = key == Key.Escape && type == KeyEventType.KeyDown
