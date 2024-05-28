package ch.epfl.cs311.wanderwave.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.utils.LocationUpdatesService
import ch.epfl.cs311.wanderwave.model.utils.findClosestBeacon
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.components.map.BeaconMapMarker
import ch.epfl.cs311.wanderwave.ui.components.map.WanderwaveGoogleMap
import ch.epfl.cs311.wanderwave.viewmodel.MapViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalPermissionsApi::class)
fun needToRequestPermissions(permissionState: MultiplePermissionsState): Boolean {
  return permissionState.permissions.any { !it.status.isGranted }
}

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(navigationActions: NavigationActions, viewModel: MapViewModel) {
  val context = LocalContext.current
  val cameraPositionState: CameraPositionState = rememberCameraPositionState {}
  val mapIsLoaded = remember { mutableStateOf(false) }
  val locationState = remember { mutableStateOf<Location?>(null) }

  val permissionState =
      rememberMultiplePermissionsState(
          listOf(
              Manifest.permission.ACCESS_COARSE_LOCATION,
              Manifest.permission.ACCESS_FINE_LOCATION,
              Manifest.permission.FOREGROUND_SERVICE,
              Manifest.permission.POST_NOTIFICATIONS))

  WanderwaveGoogleMap(
      cameraPositionState = cameraPositionState,
      locationEnabled = permissionState.allPermissionsGranted,
      locationSource = viewModel.locationSource,
      modifier = Modifier.testTag("mapScreen"),
      onMapLoaded = { mapIsLoaded.value = true }) {
        MapContent(navigationActions, viewModel)
      }

  if (needToRequestPermissions(permissionState)) {
    AskForPermissions(permissionState)
  } else {
    LaunchedEffect(true) {
      val intent = Intent(context, LocationUpdatesService::class.java)
      context.startService(intent)
    }

    DisposableEffect(Unit) {
      val receiver = createLocationReceiver(locationState, viewModel)
      val filter = IntentFilter(LocationUpdatesService.ACTION_LOCATION_BROADCAST)
      LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter)

      onDispose { LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver) }
    }

    LaunchedEffect(mapIsLoaded.value) {
      val location = viewModel.getLastKnownLocation(context)
      if (location != null && mapIsLoaded.value) {
        moveCamera(cameraPositionState, location, viewModel.cameraPosition.value)
      }
    }
  }
}

fun createLocationReceiver(
    locationState: MutableState<Location?>,
    viewModel: MapViewModel
): BroadcastReceiver {
  return object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

      val latitude = intent?.getDoubleExtra(LocationUpdatesService.EXTRA_LATITUDE, 0.0)
      val longitude = intent?.getDoubleExtra(LocationUpdatesService.EXTRA_LONGITUDE, 0.0)
      if (latitude != null && longitude != null) {
        locationState.value = Location(latitude, longitude)
        if (context != null) {
          viewModel.loadBeacons(context, locationState.value!!)
        }
        val tempBeacon = findClosestBeacon(locationState.value!!, viewModel.beaconList.value)
        if (tempBeacon != null) {
          viewModel.getRandomSong(tempBeacon.id)
          viewModel.retrieveRandomSongFromProfileAndAddToBeacon(tempBeacon.id)
        }
      }
    }
  }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AskForPermissions(permissionState: MultiplePermissionsState) {
  if (!permissionState.allPermissionsGranted) {
    AlertDialog(
        title = { Text(stringResource(id = R.string.permission_request_title)) },
        text = { Text(text = stringResource(id = R.string.permission_request_text_location)) },
        onDismissRequest = { permissionState.launchMultiplePermissionRequest() },
        confirmButton = {
          TextButton(onClick = { permissionState.launchMultiplePermissionRequest() }) {
            Text(stringResource(id = R.string.permission_request_confirm_button))
          }
        })
  }
}

@Composable
fun MapContent(navigationActions: NavigationActions, viewModel: MapViewModel) {
  val beacons: List<Beacon> = viewModel.uiState.collectAsStateWithLifecycle().value.beacons
  DisplayBeacons(navigationActions, beacons = beacons)
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
/**
 * This is a Composable function that displays the beacons on the map. It takes a list of beacons as
 * input and adds a marker for each beacon on the map.
 *
 * @param beacons The list of beacons to be displayed on the map.
 */
@Composable
fun DisplayBeacons(navigationActions: NavigationActions, beacons: List<Beacon>) {
  // Create each beacon from the list
  beacons.forEach() {
    BeaconMapMarker(
        it.location.toLatLng(),
        title = it.id,
        onClick = { navigationActions.navigateToBeacon(it.id) })
  }
}
