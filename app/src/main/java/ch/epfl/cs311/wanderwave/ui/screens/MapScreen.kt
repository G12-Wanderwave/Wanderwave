package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.epfl.cs311.wanderwave.viewmodel.MapViewModel
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

@Composable
@Preview
fun MapScreen() {
  val viewModel : MapViewModel = hiltViewModel()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  GoogleMap() {
    uiState.beacons.forEach() {
      // Add a marker for each beacon
      val position = it.location
      Marker(
        state = MarkerState(position = position.toLatLng()),
        title = it.id
      )
      // MarkerOptions().position(LatLng(it.latitude, it.longitude)).title(it.title)
    }
  }
}
