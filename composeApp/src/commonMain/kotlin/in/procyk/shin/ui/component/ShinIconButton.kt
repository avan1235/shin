package `in`.procyk.shin.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun ShinIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    iconOutlined: ImageVector = icon,
    contentDescription: String? = null,
    size: Dp = 24.dp,
    hoveredColor: Color = LocalContentColor.current,
    notHoveredColor: Color = hoveredColor.copy(alpha = 0.6f),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(12.dp)
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null,
            )
    ) {
        Icon(
            imageVector = if (isHovered) icon else iconOutlined,
            contentDescription = contentDescription,
            modifier = Modifier.size(size),
            tint = when {
                isHovered -> hoveredColor
                else -> notHoveredColor
            }
        )
    }
}