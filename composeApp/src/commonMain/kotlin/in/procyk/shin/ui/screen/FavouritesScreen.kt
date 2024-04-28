package `in`.procyk.shin.ui.screen

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import `in`.procyk.shin.component.FavouritesComponent

@Composable
internal fun FavouritesScreen(component: FavouritesComponent) {
    val favourites by component.favourites.subscribeAsState()
    LazyColumn(

    ) {
        items(favourites) { item ->
            Card {
                Text("${item.fullUrl} -> ${item.shortUrl}")
            }
        }
    }
}