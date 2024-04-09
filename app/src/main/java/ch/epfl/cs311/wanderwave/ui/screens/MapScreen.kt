package ch.epfl.cs311.wanderwave.ui.screens

import android.content.ContentValues
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.local.BeaconEntity
import ch.epfl.cs311.wanderwave.viewmodel.MapViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

@Composable
@Preview
fun MapScreen() {
  val viewModel: MapViewModel = hiltViewModel()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  GoogleMap(modifier = Modifier.testTag("mapScreen")) { DisplayBeacons(uiState.beacons) }
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
