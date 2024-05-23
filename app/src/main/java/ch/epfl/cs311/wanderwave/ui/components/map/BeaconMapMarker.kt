package ch.epfl.cs311.wanderwave.ui.components.map

import androidx.compose.runtime.Composable
import ch.epfl.cs311.wanderwave.AppResources
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

@Composable
@GoogleMapComposable
fun BeaconMapMarker(
    position: LatLng,
    title: String? = null,
    snippet: String? = null,
    onClick: () -> Unit = {}
) {
  Marker(
      state = MarkerState(position = position),
      title = title,
      snippet = snippet,
      onClick = {
        onClick()
        true
      },
      icon = AppResources.beaconIcon ?: return)
}
