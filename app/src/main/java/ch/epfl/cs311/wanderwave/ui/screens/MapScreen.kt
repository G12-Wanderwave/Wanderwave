package ch.epfl.cs311.wanderwave.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ch.epfl.cs311.wanderwave.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties

@OptIn(ExperimentalPermissionsApi::class)
fun needToRequestPermissions(permissionState: MultiplePermissionsState): Boolean {
  return permissionState.permissions.any { !it.status.isGranted }
}

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
  fun onAlertDismissed() {
    permissionState.launchMultiplePermissionRequest()
  }

  if (needToRequestPermissions(permissionState)) {
    AlertDialog(
        title = { Text(stringResource(id = R.string.permission_request_title)) },
        text = { Text(text = stringResource(id = R.string.permission_request_text_location)) },
        onDismissRequest = { onAlertDismissed() },
        confirmButton = { TextButton(onClick = { onAlertDismissed() }) { Text("I understand") } })
  }
  GoogleMap(
      modifier = Modifier.testTag("mapScreen"),
      properties =
          MapProperties(
              isMyLocationEnabled = permissionState.allPermissionsGranted,
          ),
  ) {}
}
