package ch.epfl.cs311.wanderwave.ui.permissions

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * Composable function to request location permissions and handle different scenarios.
 *
 * @param onPermissionGranted Callback to be executed when all requested permissions are granted.
 * @param onPermissionDenied Callback to be executed when any requested permission is denied.
 * @param onPermissionsRevoked Callback to be executed when previously granted permissions are
 *   revoked.
 */
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
