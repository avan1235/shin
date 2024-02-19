package `in`.procyk.shin.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import `in`.procyk.compose.qrcode.QrData
import `in`.procyk.compose.qrcode.options.*
import `in`.procyk.compose.qrcode.rememberQrCodePainter

@Composable
internal fun QrCode(url: String) {
    val painter = rememberQrCodePainter(
        data = QrData.text(url),
    ) {
        shapes {
            ball = QrBallShape.circle()
            darkPixel = QrPixelShape.roundCorners()
            frame = QrFrameShape.roundCorners(.25f)
        }
        colors {
            dark = QrBrush.solid(Color.Black)
        }
    }
    Image(painter, "QR code")
}
