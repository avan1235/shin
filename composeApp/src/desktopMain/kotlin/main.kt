import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import `in`.procyk.shin.ShinApp

fun main() {
    application {
        Window(
            title = "Shin",
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(
                height = DEFAULT_SIZE.height,
                width = DEFAULT_SIZE.width
            ),
            icon = BitmapPainter(useResource("ic_launcher.png", ::loadImageBitmap)),
        ) {
            ShinApp()
        }
    }
}

private val DEFAULT_SIZE: DpSize = DpSize(480.dp, 640.dp)
