package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.theme.WanderwaveTheme
import ch.epfl.cs311.wanderwave.viewmodel.BeaconViewModel

@Composable
fun BeaconScreen(
    navigationActions: NavigationActions,
    viewModel: BeaconViewModel = hiltViewModel()
) {
  // id value remebered for the text field
  // Here is the id of a good beacon for testing : UAn8OUadgrUOKYagf8a2
  var id by remember { mutableStateOf("") }
  var beacon = viewModel.beacon.collectAsState(initial = null)
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally) {
    TextField(value = id, onValueChange = { id = it})
    Button(onClick = {
      viewModel.getBeaconById(id)
    }) { Text("Get Beacon") }

    BeaconInformation(beacon)
    SongList(beacon)



  }
}

@Composable
@Preview(showBackground = true)
private fun BeaconScreenPreview() {
  WanderwaveTheme { BeaconScreen() }
}

@Composable
private fun BeaconScreen() {
}

@Composable
fun BeaconInformation(beacon: State<Beacon?>) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text("Beacon", style = MaterialTheme.typography.displayMedium)
    beacon.value?.let {
      Text("Beacon: ${it.id}")
    }
    // location
    beacon.value?.let {
      val location = it.location
      Text("Location: lat:${location.latitude}, long:${location.longitude}")
    }
  }
}

@Composable
fun SongList(beacon: State<Beacon?>) {
  // songs
  beacon.value?.let {
    val tracks:List<Track> = it.tracks
    tracks.map { track ->
      Text("Track: ${track.title}")
    }
  }
}
