package ch.epfl.cs311.wanderwave.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel
import com.spotify.protocol.types.ListItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

//@Composable
//fun CollapsableLazyColumn(
//    sections: List<CollapsableSection>,
//    modifier: Modifier = Modifier
//) {
//    val collapsedState = remember(sections) { sections.map { true }.toMutableStateList() }
//    LazyColumn(modifier) {
//        sections.forEachIndexed { i, dataItem ->
//            val collapsed = collapsedState[i]
//            item(key = "header_$i") {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .clickable {
//                            collapsedState[i] = !collapsed
//                        }
//                ) {
//                    Icon(
//                        Icons.Default.run {
//                            if (collapsed)
//                                KeyboardArrowDown
//                            else
//                                KeyboardArrowUp
//                        },
//                        contentDescription = "",
//                        tint = Color.LightGray,
//                    )
//                    Text(
//                        dataItem.title,
//                        fontWeight = FontWeight.Bold,
//                        modifier = Modifier
//                            .padding(vertical = 10.dp)
//                            .weight(1f)
//                    )
//                }
//                Divider()
//            }
//            if (!collapsed) {
//                items(dataItem.rows) { row ->
//                    Row {
//                        Spacer(modifier = Modifier.size(MaterialIconDimension.dp))
//                        Text(
//                            row,
//                            modifier = Modifier
//                                .padding(vertical = 10.dp)
//                        )
//                    }
//                    Divider()
//                }
//            }
//        }
//    }
//}
//
//data class CollapsableSection(val title: String, val rows: List<String>)
//
//const val MaterialIconDimension = 24f
//
//@Preview
//@Composable
//fun previewScreen(){
//    CollapsableLazyColumn(
//        sections = listOf(
//            CollapsableSection(
//                title = "Fruits A",
//                rows = listOf("Apple", "Apricots", "Avocado")
//            ),
//            CollapsableSection(
//                title = "Fruits B",
//                rows = listOf("Banana", "Blackberries", "Blueberries")
//            ),
//            CollapsableSection(
//                title = "Fruits C",
//                rows = listOf("Cherimoya", "Cantaloupe", "Cherries", "Clementine")
//            ),
//        ),
//    )
//}
@Composable
fun SelectSongScreen(navActions:NavigationActions,
                     list: List<ListItem> ) {
    LazyColumn {
        items(list, key = { listItem -> listItem.id }) { listItem ->
            TrackItem2(listItem,navActions) // TODO: modify this, so that we are using the TrackItem from @joriba, and dont
            // have duplicated code
        }
    }

}
// TODO: modify this, so that we are using the TrackItem from @joriba, and dont have duplicated code
@Composable
fun TrackItem2(listItem: ListItem,navActions: NavigationActions) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val  childrenList by viewModel.childrenList.collectAsState()

    Card(
        colors =
        CardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            CardDefaults.cardColors().contentColor,
            CardDefaults.cardColors().disabledContainerColor,
            CardDefaults.cardColors().disabledContentColor),
        modifier = Modifier
            .height(80.dp)
            .fillMaxWidth()
            .padding(4.dp)
            .clickable {},
        onClick = { if (listItem.hasChildren){
            viewModel.retrieveChild(listItem)

            Log.d("dasadsadas",childrenList.toString())
        }

        }
    ) {
        Row {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = listItem.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium)
                Text(
                    text = listItem.subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}