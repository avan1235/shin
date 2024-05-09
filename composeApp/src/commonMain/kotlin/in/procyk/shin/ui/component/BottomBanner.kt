package `in`.procyk.shin.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp

data class BottomBannerItem(
    val url: String,
    val icon: ImageVector,
) {
}

@Composable
internal fun BottomBanner(
    title: String?,
    vararg items: BottomBannerItem,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        title?.let { Text(it) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            val uriHandler = LocalUriHandler.current
            for (item in items) {
                ShinIconButton(
                    onClick = { uriHandler.openUri(item.url) },
                    icon = item.icon,
                )
            }
        }
    }
}
