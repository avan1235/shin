package `in`.procyk.shin.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import `in`.procyk.compose.camera.permission.CameraPermissionState
import `in`.procyk.shin.component.ScanQRCodeComponent
import `in`.procyk.shin.ui.component.QRCodeReader
import `in`.procyk.shin.ui.component.ShinTextButton

@Composable
internal fun ScanQRCodeScreen(
    component: ScanQRCodeComponent,
    permission: CameraPermissionState,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            !permission.isAvailable -> Text("Camera not available")
            !permission.permission.isGranted -> {
                ShinTextButton(
                    text = "Request Camera Permission",
                    onClick = { component.onRequestCameraPermission(permission) },
                )
            }

            permission.permission.isGranted -> QRCodeReader(
                onScanned = { component.onScanned(it.first()) },
                onError = component::onScanError,
                onCancel = component::onCancel,
            )
        }
    }
}