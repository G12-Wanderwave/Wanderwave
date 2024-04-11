package ch.epfl.cs311.wanderwave.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalPermissionsApi::class)
fun needToRequestPermissions(permissionState: MultiplePermissionsState): Boolean {
  return permissionState.permissions.any { !it.status.isGranted }
}

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(navigationActions: NavigationActions, viewModel: MapViewModel = hiltViewModel()) {
  val context = LocalContext.current
  val cameraPositionState: CameraPositionState = rememberCameraPositionState {}
  val mapIsLoaded = remember { mutableStateOf(false) }

  val permissionState =
      rememberMultiplePermissionsState(
          listOf(
              Manifest.permission.ACCESS_COARSE_LOCATION,
              Manifest.permission.ACCESS_FINE_LOCATION,
          ))

  // remember the camera position when navigating away
  DisposableEffect(Unit) {
    onDispose { viewModel.cameraPosition.value = cameraPositionState.position }
  }

  // if we have permission, show the location, otherwise show the map without location
  if (permissionState.allPermissionsGranted) {
    WanderwaveGoogleMap(
        cameraPositionState,
        viewModel.locationSource,
        onMapLoaded = {
          println("Map is loaded!")
          mapIsLoaded.value = true
        })
  } else {
    WanderwaveGoogleMap(cameraPositionState)
  }

  if (needToRequestPermissions(permissionState)) {
    AskForPermissions(permissionState)
  } else {
    // if we have location permissions, move the camera to the last known location **once**
    val location = viewModel.getLastKnownLocation(context)
    LaunchedEffect(location != null, mapIsLoaded.value) {
      if (location != null && mapIsLoaded.value) {
        moveCamera(cameraPositionState, location, viewModel.cameraPosition.value)
      }
    }
  }
}

@Composable
fun WanderwaveGoogleMap(cameraPositionState: CameraPositionState) {
  val context = LocalContext.current
  GoogleMap(
      modifier = Modifier.testTag("mapScreen"),
      properties =
          MapProperties(
              mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)),
  ) {
    MapContent()
  }
}

@Composable
fun WanderwaveGoogleMap(
    cameraPositionState: CameraPositionState,
    locationSource: LocationSource,
    onMapLoaded: () -> Unit
) {
  val context = LocalContext.current
  GoogleMap(
      modifier = Modifier.testTag("mapScreen"),
      properties =
          MapProperties(
              isMyLocationEnabled = true,
              mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)),
      locationSource = locationSource,
      cameraPositionState = cameraPositionState,
      onMapLoaded = onMapLoaded) {
        MapContent()
      }
}

@Composable
fun MapContent() {
  val epfl = LatLng(46.518831258, 6.559331096)
  Marker(state = MarkerState(position = epfl), title = "Marker at EPFL")
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AskForPermissions(permissionState: MultiplePermissionsState) {
  fun onAlertDismissed() {
    permissionState.launchMultiplePermissionRequest()
  }

  AlertDialog(
      title = { Text(stringResource(id = R.string.permission_request_title)) },
      text = { Text(text = stringResource(id = R.string.permission_request_text_location)) },
      onDismissRequest = { onAlertDismissed() },
      confirmButton = {
        TextButton(onClick = { onAlertDismissed() }) {
          Text(stringResource(id = R.string.permission_request_confirm_button))
        }
      })
}

fun moveCamera(
    cameraPositionState: CameraPositionState,
    location: LatLng,
    currentCameraPosition: CameraPosition?
) {
  if (currentCameraPosition != null) {
    cameraPositionState.move(CameraUpdateFactory.newCameraPosition(currentCameraPosition))
  } else {
    cameraPositionState.move(
        CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(location, 15f)))
  }
}
