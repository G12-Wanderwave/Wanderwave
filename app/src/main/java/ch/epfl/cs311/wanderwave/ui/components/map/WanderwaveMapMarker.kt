package ch.epfl.cs311.wanderwave.ui.components.map

import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

@Composable
@GoogleMapComposable
fun WanderwaveMapMarker(position: LatLng, title: String? = null, snippet: String? = null) {
  Marker(state = MarkerState(position = position), title = title, snippet = snippet)
}
