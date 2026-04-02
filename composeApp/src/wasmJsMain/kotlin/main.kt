import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import `in`.procyk.shin.ShinApp

fun main() {
    val composeTarget = document.body ?: error("no <body>")
    ComposeViewport(composeTarget) {
        ShinApp()
    }
}
