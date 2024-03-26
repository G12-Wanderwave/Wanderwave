package ch.epfl.cs311.wanderwave.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import ch.epfl.cs311.wanderwave.ui.permissions.RequestLocationPermission
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
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
  Text(text = "Latitude: ${location?.latitude}, Longitude: ${location?.longitude}")
  LocationUpdatesScreen { location = it }

  GoogleMap {
    if (location != null) {
      Marker(
        state = MarkerState(position = location!!)
      )
    }
  }
}

@SuppressLint("MissingPermission")
@Composable
fun LocationUpdatesScreen(onLocationChange: (LatLng) -> Unit) {
  var permission by remember { mutableStateOf(false) }
  RequestLocationPermission(
    onPermissionGranted = { permission = true},
    onPermissionDenied = { /*TODO*/ },
    onPermissionsRevoked = { /*TODO*/ }
  )

  LocationUpdatesContent(
    usePreciseLocation = permission,
    onLocationChange
  )
}

@RequiresPermission(
  anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)
@Composable
fun LocationUpdatesContent(
  usePreciseLocation: Boolean,
  onLocationChange: (LatLng) -> Unit
) {
  // The location request that defines the location updates
  var locationRequest by remember {
    mutableStateOf<LocationRequest?>(null)
  }

  // Only register the location updates effect when we have a request
  if (locationRequest != null) {
    LocationUpdatesEffect(locationRequest!!) { result ->
      // For each result update the text
      for (currentLocation in result.locations) {
        onLocationChange(LatLng(currentLocation.latitude, currentLocation.longitude))
      }
    }
  }
  val priority = if (usePreciseLocation) {
    Priority.PRIORITY_HIGH_ACCURACY
  } else {
    Priority.PRIORITY_BALANCED_POWER_ACCURACY
  }
  locationRequest = LocationRequest.Builder(priority, TimeUnit.SECONDS.toMillis(1)).build()
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
  lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
  onUpdate: (result: LocationResult) -> Unit,
) {
  val context = LocalContext.current
  val currentOnUpdate by rememberUpdatedState(newValue = onUpdate)

  // Whenever on of these parameters changes, dispose and restart the effect.
  DisposableEffect(locationRequest, lifecycleOwner) {
    val locationClient = LocationServices.getFusedLocationProviderClient(context)
    val locationCallback: LocationCallback = object : LocationCallback() {
      override fun onLocationResult(result: LocationResult) {
        currentOnUpdate(result)
      }
    }
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_START) {
        locationClient.requestLocationUpdates(
          locationRequest, locationCallback, Looper.getMainLooper(),
        )
      } else if (event == Lifecycle.Event.ON_STOP) {
        locationClient.removeLocationUpdates(locationCallback)
      }
    }

    // Add the observer to the lifecycle
    lifecycleOwner.lifecycle.addObserver(observer)

    // When the effect leaves the Composition, remove the observer
    onDispose {
      locationClient.removeLocationUpdates(locationCallback)
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }
}
