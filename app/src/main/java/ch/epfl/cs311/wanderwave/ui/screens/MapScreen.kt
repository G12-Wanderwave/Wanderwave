package ch.epfl.cs311.wanderwave.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.annotation.RequiresPermission
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.viewmodel.MapViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalPermissionsApi::class)
fun needToRequestPermissions(permissionState: MultiplePermissionsState): Boolean {
  return permissionState.permissions.any { !it.status.isGranted }
}

@RequiresPermission(
    allOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
fun getLastKnownLocation(context: Context): LatLng? {
  val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
  var location: Location? = null

  // Get the best last known location from either GPS or Network provider
  val providers = locationManager.getProviders(true)
  for (provider in providers) {
    val l = locationManager.getLastKnownLocation(provider) ?: continue
    if (location == null || l.accuracy < location.accuracy) {
      location = l
    }
  }
  return location?.let { LatLng(it.latitude, it.longitude) }
}

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(navigationActions: NavigationActions, viewModel: MapViewModel = hiltViewModel()) {
  val permissionState =
      rememberMultiplePermissionsState(
          listOf(
              Manifest.permission.ACCESS_COARSE_LOCATION,
              Manifest.permission.ACCESS_FINE_LOCATION,
          ))

  fun onAlertDismissed() {
    permissionState.launchMultiplePermissionRequest()
  }

  val cameraPositionState: CameraPositionState = rememberCameraPositionState() {}
  val mapIsLoaded = remember { mutableStateOf(false) }

  DisposableEffect(Unit) {
    onDispose { viewModel.cameraPosition.value = cameraPositionState.position }
  }

  GoogleMap(
      modifier = Modifier.testTag("mapScreen"),
      properties =
          MapProperties(
              isMyLocationEnabled = permissionState.allPermissionsGranted,
          ),
      locationSource = viewModel.locationSource,
      cameraPositionState = cameraPositionState,
      onMapLoaded = {
        println("Map is loaded!")
        mapIsLoaded.value = true
      }) {}

  if (needToRequestPermissions(permissionState)) {
    AlertDialog(
        title = { Text(stringResource(id = R.string.permission_request_title)) },
        text = { Text(text = stringResource(id = R.string.permission_request_text_location)) },
        onDismissRequest = { onAlertDismissed() },
        confirmButton = {
          TextButton(onClick = { onAlertDismissed() }) {
            Text(stringResource(id = R.string.permission_request_confirm_button))
          }
        })
  } else {
    val location = getLastKnownLocation(LocalContext.current)

    LaunchedEffect(location != null, mapIsLoaded.value) {
      if (location != null && mapIsLoaded.value) {
        val cameraPosition = viewModel.cameraPosition.value
        if (cameraPosition != null) {
          cameraPositionState.move(CameraUpdateFactory.newCameraPosition(cameraPosition))
        } else {
          cameraPositionState.move(
              CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(location, 15f)))
        }
      }
    }
  }
}
