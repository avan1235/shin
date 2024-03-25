import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import `in`.procyk.shin.ShinApp
import `in`.procyk.shin.component.ShinAppComponentContext
import `in`.procyk.shin.component.ShinComponentImpl
import `in`.procyk.shin.runOnUiThread

@OptIn(ExperimentalDecomposeApi::class)
fun main() {
    val lifecycle = LifecycleRegistry()
    val component = runOnUiThread {
        ShinComponentImpl(
            ShinAppComponentContext(),
            DefaultComponentContext(lifecycle)
        )
    }
    application {
        val windowState = rememberWindowState(
            height = DEFAULT_SIZE.height,
            width = DEFAULT_SIZE.width
        )

        LifecycleController(lifecycle, windowState)

        Window(
            title = "Shin",
            onCloseRequest = ::exitApplication,
            state = windowState,
            icon = BitmapPainter(useResource("ic_launcher.png", ::loadImageBitmap)),
        ) {
            ShinApp(component)
        }
    }
}

private val DEFAULT_SIZE: DpSize = DpSize(480.dp, 640.dp)