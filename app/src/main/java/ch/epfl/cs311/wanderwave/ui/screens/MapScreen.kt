package ch.epfl.cs311.wanderwave.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
@Preview
fun MapScreen() {
  val permissionState =
      rememberMultiplePermissionsState(
          listOf(
              Manifest.permission.ACCESS_COARSE_LOCATION,
              Manifest.permission.ACCESS_FINE_LOCATION,
          ))

  LaunchedEffect(key1 = permissionState) {
    // Filter permissions that need to be requested.
    val permissionsToRequest = permissionState.permissions.filter { !it.status.isGranted }

    // If there are permissions to request, launch the permission request.
    if (permissionsToRequest.isNotEmpty()) permissionState.launchMultiplePermissionRequest()
  }

  Column(modifier = Modifier.testTag("mapScreen")) {
    GoogleMap(
        properties =
            MapProperties(
                isMyLocationEnabled = permissionState.allPermissionsGranted,
            ),
    ) {}
  }
}
