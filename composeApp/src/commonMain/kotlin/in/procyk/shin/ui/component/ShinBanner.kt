package `in`.procyk.shin.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import shin.composeapp.generated.resources.Mansalva_Regular
import shin.composeapp.generated.resources.PoetsenOne_Regular
import shin.composeapp.generated.resources.Res

@Composable
internal fun ShinBanner(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Shin",
            fontFamily = FontFamily(Font(Res.font.Mansalva_Regular)),
            style = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.primary),
            fontSize = 64.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "Shorten Your URL with Kotlin",
            fontFamily = FontFamily(Font(Res.font.PoetsenOne_Regular)),
            style = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
