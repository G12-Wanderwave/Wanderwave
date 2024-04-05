package ch.epfl.cs311.wanderwave.viewmodel

import android.Manifest
import android.content.Context
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.CompositionLocal
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class MapViewModel @Inject constructor() : ViewModel() {
  private val _uiState = MutableStateFlow(MapUiState())
  val uiState: StateFlow<MapUiState> = _uiState

  @RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
  )
  fun requestLocationUpdates(context: Context, locationRequest: LocationRequest) {
    val locationClient = LocationServices.getFusedLocationProviderClient(context)
    val locationCallback: LocationCallback =
      object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
          _uiState.value = MapUiState(userLocation = result.lastLocation?.let { LatLng(it.latitude, it.longitude) })
        }
      }
    locationClient.requestLocationUpdates(
      locationRequest,
      locationCallback,
      Looper.getMainLooper(),
    )
  }
}

data class MapUiState(val userLocation: LatLng? = null)