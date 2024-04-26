package ch.epfl.cs311.wanderwave.ui.components.map

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import ch.epfl.cs311.wanderwave.R
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

private fun bitmapFromResource(resourceId: Int, context: Context): Bitmap {
  val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
  return bitmap
}

private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
  return Bitmap.createScaledBitmap(bitmap, width, height, false)
}

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
      icon =
          BitmapDescriptorFactory.fromBitmap(
              resizeBitmap(bitmapFromResource(R.drawable.beaconlogo, context = LocalContext.current), 100, 100)))
}
