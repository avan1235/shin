import androidx.compose.ui.window.ComposeViewport
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import kotlinx.browser.document
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
    val composeTarget = document.body ?: error("no <body>")
    ComposeViewport(composeTarget) {
        ShinApp(component)
    }
}
