import androidx.compose.ui.window.ComposeUIViewController
import `in`.procyk.shin.component.ShinComponent

fun MainViewController(component: ShinComponent) = ComposeUIViewController {
    ShinApp(component)
}
