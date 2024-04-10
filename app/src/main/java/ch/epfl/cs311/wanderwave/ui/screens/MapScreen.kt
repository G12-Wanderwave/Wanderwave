package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.viewmodel.MapViewModel
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

@Composable
@Preview
fun MapScreen() {

    val viewModel: MapViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  // TODO: Replace with actual data once implemented
  val mockBeacons =
      listOf<Beacon>(
          Beacon("INM", Location(46.51857556996283, 6.5631609607190775)),
          Beacon("BC", Location(46.51857417773428, 6.5619195033506434)),
          Beacon("STCC", Location(46.52298529087412, 6.564644391110982)),
          Beacon("RLC", Location(46.51846723837138, 6.568149323030634)))

  GoogleMap(modifier = Modifier.testTag("mapScreen")) { DisplayBeacons(beacons = mockBeacons) }
}

/**
 * This is a Composable function that displays the beacons on the map. It takes a list of beacons as
 * input and adds a marker for each beacon on the map.
 *
 * @param beacons The list of beacons to be displayed on the map.
 */
@Composable
fun DisplayBeacons(beacons: List<Beacon>) {

  // Add a marker for each beacon
  // source for the icon: https://www.svgrepo.com/svg/448258/waypoint
  // val customIcon = BitmapDescriptorFactory.fromResource(R.drawable.waypoint)
  beacons.forEach() {
    Marker(
        state = MarkerState(position = it.location.toLatLng()),
        title = it.id,
        // icon = customIcon,
    )
  }
}
