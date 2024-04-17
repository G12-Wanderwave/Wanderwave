package ch.epfl.cs311.wanderwave.ui.components.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.ui.screens.MapContent
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings

@Composable
fun WanderwaveGoogleMap(
  cameraPositionState: CameraPositionState,
  modifier: Modifier = Modifier,
  onMapLoaded: () -> Unit = {},
  controlsEnabled: Boolean = true,
  locationEnabled: Boolean = false,
  locationSource: LocationSource? = null,
  content: @Composable () -> Unit = {}
) {
  val context = LocalContext.current
  GoogleMap(
    modifier = modifier,
    locationSource = locationSource,
    cameraPositionState = cameraPositionState,
    onMapLoaded = onMapLoaded,
    properties =
      MapProperties(
        isMyLocationEnabled = locationEnabled,
        mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)),
    uiSettings = MapUiSettings(
      zoomControlsEnabled = controlsEnabled,
      compassEnabled = controlsEnabled,
      myLocationButtonEnabled = controlsEnabled,
      scrollGesturesEnabled = controlsEnabled,
      zoomGesturesEnabled = controlsEnabled,
      tiltGesturesEnabled = controlsEnabled,
    )
  ) { content() }
}