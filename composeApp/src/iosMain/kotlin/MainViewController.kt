import androidx.compose.ui.window.ComposeUIViewController
import `in`.procyk.shin.ShinApp
import `in`.procyk.shin.component.ShinAppComponent

fun MainViewController(component: ShinAppComponent) = ComposeUIViewController {
    ShinApp(component)
}
