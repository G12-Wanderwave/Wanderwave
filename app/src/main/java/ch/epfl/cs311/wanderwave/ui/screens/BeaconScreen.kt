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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.epfl.cs311.wanderwave.model.data.Location
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
  var id by remember { mutableStateOf("UAn8OUadgrUOKYagf8a2") }
  val beacon = viewModel.beacon.collectAsState(initial = null)
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    TextField(value = id, onValueChange = { id = it }, placeholder = { Text("Beacon ID") })
    Button(onClick = {
      viewModel.getBeaconById(id)
    }) { Text("Get Beacon") }

    if (beacon.value != null) {
      BeaconScreen(beacon.value!!)
    } else {
      Text("Beacon not found")
    }
  }
}

@Composable
@Preview(showBackground = true)
private fun BeaconScreenPreview() {
  val previewBeacon =
      Beacon(
          id = "a",
          location = Location(latitude = 46.519962, longitude = 6.633597, name = "EPFL"),
          tracks =
              listOf(
                  Track("a", "Never gonna give you up", "Rick Astley"),
                  Track("b", "Take on me", "A-ha"),
                  Track("c", "Africa", "Toto"),
              ),
      )
  WanderwaveTheme { BeaconScreen(previewBeacon) }
}

@Composable
private fun BeaconScreen(beacon: Beacon) {
  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        BeaconInformation(beacon.location)
        SongList(beacon)
      }
}

@Composable
fun BeaconInformation(location: Location) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text("Beacon", style = MaterialTheme.typography.displayMedium)
    val locationText = if (location.name != "") {
      "${location.name} (${location.latitude}, ${location.longitude})"
    } else {
      "(${location.latitude}, ${location.longitude})"
    }
    Text("at $locationText", style = MaterialTheme.typography.titleMedium)
  }
}

@Composable
fun SongList(beacon: Beacon) {
  // songs
  beacon.tracks.forEach() { track ->
    Text("Track: ${track.title} by ${track.artist}")
  }
}
