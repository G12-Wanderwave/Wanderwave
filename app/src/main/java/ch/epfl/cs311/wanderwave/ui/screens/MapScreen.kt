package ch.epfl.cs311.wanderwave.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import ch.epfl.cs311.wanderwave.ui.permissions.RequestLocationPermission
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import java.util.concurrent.TimeUnit

@SuppressLint("MissingPermission")
@Composable
@Preview
fun MapScreen() {
  var location by remember { mutableStateOf<LatLng?>(null) }
  LocationUpdatesScreen { location = it }
  GoogleMap {
    if (location != null) {
      Marker(state = MarkerState(position = location!!))
    }
  }
}

@SuppressLint("MissingPermission")
@Composable
fun LocationUpdatesScreen(onLocationChange: (LatLng) -> Unit) {
  var locationRequest : LocationRequest? by remember { mutableStateOf(null) }
  RequestLocationPermission(
      onPermissionGranted = {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, TimeUnit.SECONDS.toMillis(1)).build()
      },
      onPermissionDenied = { /*TODO*/},
      onPermissionsRevoked = { /*TODO*/})

  // Only register the location updates effect when we have a request
if (locationRequest != null) {
    LocationUpdatesEffect(locationRequest!!) { result ->
      // For each result update the text
      for (currentLocation in result.locations) {
        onLocationChange(LatLng(currentLocation.latitude, currentLocation.longitude))
      }
    }
  }
}

/**
 * An effect that request location updates based on the provided request and ensures that the
 * updates are added and removed whenever the composable enters or exists the composition.
 */
@RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)
@Composable
fun LocationUpdatesEffect(
    locationRequest: LocationRequest,
    onUpdate: (result: LocationResult) -> Unit,
) {
  val context = LocalContext.current

  val locationClient = LocationServices.getFusedLocationProviderClient(context)
  val locationCallback: LocationCallback =
      object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
          onUpdate(result)
        }
      }
  locationClient.requestLocationUpdates(
      locationRequest,
      locationCallback,
      Looper.getMainLooper(),
  )
}
