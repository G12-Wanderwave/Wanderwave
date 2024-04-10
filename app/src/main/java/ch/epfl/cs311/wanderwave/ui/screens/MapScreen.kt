package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import ch.epfl.cs311.wanderwave.model.data.Beacon
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.epfl.cs311.wanderwave.viewmodel.MapViewModel
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

@Composable
@Preview
fun MapScreen() {
  val viewModel: MapViewModel = hiltViewModel()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  //TODO: Make sure the beacons are filtered to be within the radius
  GoogleMap(modifier = Modifier.testTag("mapScreen")) { DisplayBeacons(beacons = uiState.beacons) }
}

/**
 * This is a Composable function that displays the beacons on the map. It takes a list of beacons as
 * input and adds a marker for each beacon on the map.
 *
 * @param beacons The list of beacons to be displayed on the map.
 */
@Composable
fun DisplayBeacons(beacons: List<Beacon>) {
  //Create each beacon from the list
  beacons.forEach() {
    Marker(
        state = MarkerState(position = it.location.toLatLng()),
        title = it.id,
    )
  }
}
