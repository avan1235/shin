package `in`.procyk.shin.ui

import androidx.compose.foundation.Image
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import `in`.procyk.compose.qrcode.QrData
import `in`.procyk.compose.qrcode.options.*
import `in`.procyk.compose.qrcode.rememberQrCodePainter

@Composable
internal fun QrCode(url: String) {
    val primaryColor = MaterialTheme.colors.primary
    val secondaryColor = MaterialTheme.colors.secondary
    val painter = rememberQrCodePainter(
        data = QrData.text(url),
    ) {
        shapes {
            ball = QrBallShape.circle()
            darkPixel = QrPixelShape.roundCorners()
            frame = QrFrameShape.roundCorners(.25f)
        }
        colors {
            dark = QrBrush.brush {
                Brush.linearGradient(
                    0f to secondaryColor,
                    1f to primaryColor,
                    end = Offset(it, it)
                )
            }
        }
    }
    Image(painter, "QR code")
}
