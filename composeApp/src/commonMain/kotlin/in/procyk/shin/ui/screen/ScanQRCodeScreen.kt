package `in`.procyk.shin.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import `in`.procyk.compose.camera.permission.CameraPermissionState
import `in`.procyk.shin.component.ScanQRCodeComponent
import `in`.procyk.shin.ui.QRCodeReader
import `in`.procyk.shin.ui.ShinTextButton

@Composable
internal fun ScanQRCodeScreen(
    componenet: ScanQRCodeComponent,
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
                    onClick = { componenet.onRequestCameraPermission(permission) },
                )
            }

            permission.permission.isGranted -> QRCodeReader(
                onScanned = { componenet.onScanned(it.first()) },
                onError = componenet::onScanError,
                onCancel = componenet::onCancel,
            )
        }
    }
}