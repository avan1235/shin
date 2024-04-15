package `in`.procyk.shin.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import `in`.procyk.compose.camera.qr.QRCodeScanner
import `in`.procyk.compose.camera.qr.QRResult

@Composable
internal fun BoxScope.QRCodeReader(
    onScanned: (List<String>) -> Unit,
    onError: () -> Unit,
    onCancel: () -> Unit,
) {
    var isCameraLoading by remember { mutableStateOf(true) }
    QRCodeScanner(
        onResult = { currentResult ->
            when (currentResult) {
                is QRResult.QRError -> onError().let { false }
                is QRResult.QRSuccess -> onScanned(currentResult.nonEmptyCodes).let { true }
            }
        },
        onIsLoadingChange = { isCameraLoading = it },
        backgroundColor = MaterialTheme.colorScheme.background,
    )
    QRCodeCameraHole()
    Box(
        modifier = Modifier
            .padding(32.dp)
            .align(Alignment.BottomCenter),
    ) {
        ShinTextButton(
            text = "Cancel",
            onClick = onCancel,
        )
    }
    AnimatedVisibility(
        visible = isCameraLoading,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun QRCodeCameraHole(
    holePercent: Float = 0.7f,
    borderColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
    border: Dp = 4.dp,
    backgroundAlpha: Float = 0.8f,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
) {
    Canvas(
        modifier = Modifier.fillMaxSize(),
        onDraw = {
            val holeSize = size.minDimension * holePercent
            val borderPx = border.roundToPx()
            val borderSize = holeSize + 2 * borderPx
            val halfBorderSize = borderSize / 2f
            clipPath(
                path = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(center, holeSize / 2f),
                            cornerRadius = CornerRadius(holeSize / 20f)
                        )
                    )
                },
                clipOp = ClipOp.Difference
            ) {
                drawRect(
                    color = backgroundColor,
                    alpha = backgroundAlpha,
                )
                drawRoundRect(
                    color = borderColor,
                    size = Size(borderSize, borderSize),
                    topLeft = center.minus(Offset(halfBorderSize, halfBorderSize)),
                    cornerRadius = CornerRadius(borderSize / 20f + 3f)
                )
            }
        })
}