package ch.epfl.cs311.wanderwave.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
//@Composable
//fun SelectSongScreen(navActions: NavigationActions, viewModel: ProfileViewModel) {
//    val mainList by viewModel.spotifySubsectionList.collectAsState()
//    val childrenList by viewModel.childrenList.collectAsState()
//
//    var displayedList by remember { mutableStateOf(mainList) }
//
//    LaunchedEffect(mainList) {
//        displayedList = mainList
//    }
//
//    LaunchedEffect(childrenList) {
//        displayedList = childrenList
//    }
//
//    LazyColumn {
//        items(displayedList, key = { it.id }) { listItem ->
//            TrackItem(listItem, navActions, onClick = {
//                if (listItem.hasChildren) {
//                    viewModel.retrieveChild(listItem)
//                }else{
//                    viewModel.addTrackToList("TOP SONGS", Track(listItem.id, listItem.title, listItem.subtitle))
//                    navActions.goBack()
//                }
//            })
//        }
//    }
//    Button(onClick = {
//        Log.d("contentOfList",displayedList.toString())
//        Log.d("childrenList",childrenList.toString())
//    }) {
//        Text("Add Track to TOP SONGS List")
//    }
//}
//
//@Composable
//fun TrackItem(
//    listItem: ListItem,
//    navActions: NavigationActions,
//    onClick: () -> Unit
//) {
//    Card(
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
//            contentColor = MaterialTheme.colorScheme.onSurface,
//            disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant,
//            disabledContentColor = MaterialTheme.colorScheme.error // Example color
//        ),
//        modifier = Modifier
//            .height(80.dp)
//            .fillMaxWidth()
//            .padding(4.dp)
//            .clickable(onClick = onClick)
//    ) {
//        Row {
//            Column(modifier = Modifier.padding(8.dp)) {
//                Text(
//                    text = listItem.title,
//                    style = MaterialTheme.typography.titleMedium
//                )
//                Text(
//                    text = listItem.subtitle,
//                    style = MaterialTheme.typography.bodyMedium
//                )
//            }
//        }
//    }
//}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectSongScreen(navActions: NavigationActions, viewModel: ProfileViewModel) {
    val mainList by viewModel.spotifySubsectionList.collectAsState()
    val childrenList by viewModel.childrenList.collectAsState()

    var displayedList by remember { mutableStateOf(mainList) }

    LaunchedEffect (Unit){
        viewModel.retrieveAndAddSubsection()
    }
    LaunchedEffect(mainList) {
        displayedList = mainList
    }

    LaunchedEffect(childrenList) {
        displayedList = childrenList
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Song") },
                navigationIcon = {
                    IconButton(onClick = { navActions.goBack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        }
    ) { innerPadding ->  // This is the padding parameter you need to use
        LazyColumn(
            contentPadding = innerPadding,  // Apply the innerPadding to the LazyColumn
            modifier = Modifier.padding(all = 16.dp)  // Additional padding can still be applied here if needed
        ) {
            items(displayedList, key = { it.id }) { listItem ->
                TrackItem(listItem, navActions, onClick = {
                    if (listItem.hasChildren) {
                        viewModel.retrieveChild(listItem)
                    } else {
                        viewModel.addTrackToList("TOP SONGS", Track(listItem.id, listItem.title, listItem.subtitle))
                        navActions.goBack()
                    }
                })
            }
        }
        Button(onClick = {Log.d("pls work", viewModel.spotifySubsectionList.value.toString())}) {
            Text(text = "fewfewfewfewf")
        }
    }
}


@Composable
fun TrackItem(
    listItem: ListItem,
    navActions: NavigationActions,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.error // Example color
        ),
        modifier = Modifier
            .height(80.dp)
            .fillMaxWidth()
            .padding(4.dp)
            .clickable(onClick = onClick)
    ) {
        Row {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = listItem.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = listItem.subtitle,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
