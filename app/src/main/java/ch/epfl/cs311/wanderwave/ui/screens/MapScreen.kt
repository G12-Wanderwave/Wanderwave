package ch.epfl.cs311.wanderwave.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.epfl.cs311.wanderwave.viewmodel.MapViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
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
  val viewModel: MapViewModel = hiltViewModel()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  LocationUpdatesScreen(viewModel = viewModel)
  GoogleMap {
    if (uiState.userLocation != null) {
      Marker(state = MarkerState(position = uiState.userLocation!!))
    }
  }
}

@SuppressLint("MissingPermission")
@Composable
fun LocationUpdatesScreen(viewModel: MapViewModel) {
  val context = LocalContext.current
  RequestLocationPermission(
      onPermissionGranted = {
        val locationRequest = LocationRequest.Builder(
          Priority.PRIORITY_HIGH_ACCURACY,
          TimeUnit.SECONDS.toMillis(1)
        ).build()
        viewModel.requestLocationUpdates(context, locationRequest)
      },
      onPermissionDenied = { /*TODO*/},
      onPermissionsRevoked = { /*TODO*/})
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestLocationPermission(
  onPermissionGranted: () -> Unit,
  onPermissionDenied: () -> Unit,
  onPermissionsRevoked: () -> Unit
) {
  // Initialize the state for managing multiple location permissions.
  val permissionState =
    rememberMultiplePermissionsState(
      listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
      ))

  LaunchedEffect(key1 = permissionState) {
    // Check if all previously granted permissions are revoked.
    val allPermissionsRevoked =
      permissionState.permissions.size == permissionState.revokedPermissions.size

    // Filter permissions that need to be requested.
    val permissionsToRequest = permissionState.permissions.filter { !it.status.isGranted }

    // If there are permissions to request, launch the permission request.
    if (permissionsToRequest.isNotEmpty()) permissionState.launchMultiplePermissionRequest()

    // Execute callbacks based on permission status.
    if (allPermissionsRevoked) {
      onPermissionsRevoked()
    } else {
      if (permissionState.allPermissionsGranted) {
        onPermissionGranted()
      } else {
        onPermissionDenied()
      }
    }
  }
}
