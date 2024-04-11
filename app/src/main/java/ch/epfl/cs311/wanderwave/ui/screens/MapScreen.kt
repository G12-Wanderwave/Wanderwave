package ch.epfl.cs311.wanderwave.ui.screens

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.viewmodel.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

/**
 * A screen that displays a map if loaded, else display a CircularProgressIndicator.
 *
 * @author Menzo Bouaissi
 * @since 1.0
 * @last update 1.0
 */
@Preview
@Composable
fun MapScreen() {
  val viewModel: MapViewModel = hiltViewModel()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  val context = LocalContext.current
  val isMapReady = remember { mutableStateOf(false) }
  val googleMapState = viewModel.googleMapState.collectAsState().value
  // GoogleMap(modifier = Modifier.testTag("mapScreen")) { DisplayBeacons(uiState.beacons) }

  Box(modifier = Modifier.fillMaxSize().testTag("mapScreen"), contentAlignment = Alignment.Center) {
    AndroidView(
        modifier = Modifier.testTag("map"),
        factory = { ctx ->
          MapView(ctx).apply {
            onCreate(Bundle())
            onResume()
            getMapAsync { googleMap ->
              viewModel.setGoogleMap(googleMap)
              isMapReady.value = true
              googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
            }
          }
        },
    )

    if (!isMapReady.value) {
      CircularProgressIndicator(modifier = Modifier.testTag("CircularProgressIndicator"))
    } else {
      googleMapState?.let { googleMap ->
        val epfl = LatLng(46.518831258, 6.559331096)
        googleMap.addMarker(MarkerOptions().position(epfl).title("Marker at EPFL"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(epfl))
      }
    }
  }

  // TODO: Make sure the beacons are filtered to be within the radius

//  GoogleMap(modifier = Modifier.testTag("mapScreen")) {
//    if (!uiState.loading) DisplayBeacons(beacons = uiState.beacons)
//  }
}

/**
 * This is a Composable function that displays the beacons on the map. It takes a list of beacons as
 * input and adds a marker for each beacon on the map.
 *
 * @param beacons The list of beacons to be displayed on the map.
 */
@Composable
fun DisplayBeacons(beacons: List<Beacon>) {
  // Create each beacon from the list
  beacons.forEach() {
    Marker(
        state = MarkerState(position = it.location.toLatLng()),
        title = it.id,
    )
  }
}
