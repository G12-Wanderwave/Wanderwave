package ch.epfl.cs311.wanderwave.viewmodel

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.annotation.RequiresPermission
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MapViewModel
@Inject
constructor(val locationSource: LocationSource, private val beaconRepository: BeaconRepository) :
    ViewModel() {
  val cameraPosition = MutableLiveData<CameraPosition?>()

  private val _uiState = MutableStateFlow(BeaconListUiState(loading = true))
  val uiState: StateFlow<BeaconListUiState> = _uiState

  init {
    observeBeacons()
  }

  private fun observeBeacons() {
    viewModelScope.launch {
      beaconRepository.getAll().collect { beacons ->
        _uiState.value = BeaconListUiState(beacons = beacons, loading = false)
      }
    }
  }

  @RequiresPermission(
      allOf =
          [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
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
}

data class BeaconListUiState(val beacons: List<Beacon> = listOf(), val loading: Boolean = false)
