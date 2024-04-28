package `in`.procyk.shin.ui.component

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.alexzhirkevich.qrose.QrData
import io.github.alexzhirkevich.qrose.options.*
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import io.github.alexzhirkevich.qrose.text

@Composable
internal fun QrCode(
    url: String,
    modifier: Modifier = Modifier,
) {
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
    Image(
        painter = painter,
        contentDescription = "QR code",
        modifier = modifier
    )
}
