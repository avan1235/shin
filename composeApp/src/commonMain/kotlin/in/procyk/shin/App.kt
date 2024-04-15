package `in`.procyk.shin

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import `in`.procyk.compose.camera.permission.rememberCameraPermissionState
import `in`.procyk.shin.component.ShinAppComponent
import `in`.procyk.shin.component.ShinAppComponent.Child
import `in`.procyk.shin.ui.screen.MainScreen
import `in`.procyk.shin.ui.screen.ScanQRCodeScreen
import `in`.procyk.shin.ui.theme.ShinTheme

@Composable
fun ShinApp(component: ShinAppComponent) {
    val permission = rememberCameraPermissionState()
    ShinTheme {
        Children(
            stack = component.stack,
            modifier = Modifier.fillMaxSize(),
            animation = stackAnimation(slide())
        ) { child ->
            Scaffold(
                snackbarHost = { SnackbarHost(component.snackbarHostState) },
                modifier = Modifier.fillMaxSize(),
            ) {
                when (val instance = child.instance) {
                    is Child.Main -> MainScreen(instance.component, permission.isAvailable)
                    is Child.ScanQRCode -> ScanQRCodeScreen(instance.component, permission)
                }
            }
        }
    }
}
