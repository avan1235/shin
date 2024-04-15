package `in`.procyk.shin.component

import com.arkivanov.decompose.ComponentContext
import `in`.procyk.compose.camera.permission.CameraPermissionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

interface ScanQRCodeComponent : Component {
    fun onRequestCameraPermission(permission: CameraPermissionState)
    
    fun onScanned(text: String)
    
    fun onScanError()
    
    fun onCancel()
}

class ScanQRCodeComponentImpl(
    appContext: ShinAppComponentContext,
    componentContext: ComponentContext,
    private val navigateOnCancel: (scanned: String?) -> Unit,
) : AbstractComponent(appContext, componentContext), ScanQRCodeComponent {

    private val requestedCameraPermissionCount = MutableStateFlow(0)

    override fun onRequestCameraPermission(permission: CameraPermissionState) {
        val count = requestedCameraPermissionCount.updateAndGet { it + 1 }
        if (count > MAX_CAMERA_PERMISSION_ASKS) {
            return toast("Can't access camera")
        }
        permission.launchRequest()
    }
    
    override fun onScanned(text: String) {
        navigateOnCancel(text)
    }

    override fun onScanError() {
        toast("Error Scanning QR Code")
    }

    override fun onCancel() {
        navigateOnCancel(null)
    }
}

private const val MAX_CAMERA_PERMISSION_ASKS = 3