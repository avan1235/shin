import androidx.compose.ui.window.CanvasBasedWindow
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import `in`.procyk.shin.ShinApp
import `in`.procyk.shin.component.ShinAppComponentContext
import `in`.procyk.shin.component.ShinAppComponentImpl

fun main() {
    val lifecycle = LifecycleRegistry()
    val component = ShinAppComponentImpl(
        ShinAppComponentContext(),
        DefaultComponentContext(lifecycle)
    )
    lifecycle.resume()
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        ShinApp(component)
    }
}
