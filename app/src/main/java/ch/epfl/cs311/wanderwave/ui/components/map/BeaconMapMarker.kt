package ch.epfl.cs311.wanderwave.ui.components.map

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import ch.epfl.cs311.wanderwave.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
    icon = BitmapDescriptorFactory.fromResource(R.drawable.beacon_logo)
  )
}
