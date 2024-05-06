package `in`.procyk.shin.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import `in`.procyk.shin.component.FavouritesComponent

@Composable
internal fun FavouritesScreen(component: FavouritesComponent) {
    val favourites by component.favourites.subscribeAsState()
    LazyColumn {
        items(favourites, key = { it.shortUrl }) { item ->
            val clipboardManager = LocalClipboardManager.current
            Card(
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .clickable { component.onFavouriteClick(clipboardManager, item.shortUrl) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { component.removeFavourite(item.shortUrl) }
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete favourite")
                    }
                    Text(
                        text = item.fullUrl,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = item.shortUrl,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}
