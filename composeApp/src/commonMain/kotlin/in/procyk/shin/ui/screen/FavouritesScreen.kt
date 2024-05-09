package `in`.procyk.shin.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import `in`.procyk.shin.component.FavouritesComponent
import `in`.procyk.shin.ui.component.ShinIconButton

@Composable
internal fun FavouritesScreen(component: FavouritesComponent) {
    val favourites by component.favourites.subscribeAsState()
    if (favourites.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No Favourites")
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(favourites, key = { it.shortUrl }) { item ->
                val clipboardManager = LocalClipboardManager.current
                Card(
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { component.onFavouriteClick(clipboardManager, item.shortUrl) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ShinIconButton(
                            onClick = { component.removeFavourite(item.shortUrl) },
                            icon = Icons.Filled.Delete,
                            iconOutlined = Icons.Outlined.Delete,
                            contentDescription = "Delete favourite",
                            hoveredColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                            notHoveredColor = LocalContentColor.current,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = item.fullUrl,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(2f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.shortUrl,
                                style = MaterialTheme.typography.bodyMedium.let {
                                    it.copy(color = it.color.copy(alpha = 0.3f))
                                },
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
