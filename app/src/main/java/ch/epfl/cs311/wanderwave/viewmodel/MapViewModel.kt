package ch.epfl.cs311.wanderwave.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location as Location1
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import ch.epfl.cs311.wanderwave.model.utils.createNearbyBeacons
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MapViewModel
@Inject
constructor(
    @ApplicationContext private val context: Context,
    val locationSource: LocationSource,
    private val beaconRepository: BeaconRepository
) : ViewModel() {
  val cameraPosition = MutableLiveData<CameraPosition?>()

  private val _uiState = MutableStateFlow(BeaconListUiState(loading = true))
  val uiState: StateFlow<BeaconListUiState> = _uiState

  private var _beaconList = MutableStateFlow<List<Beacon>>(emptyList())
  val beaconList: StateFlow<List<Beacon>> = _beaconList

  private val _areBeaconsLoaded = MutableStateFlow(false)

  init {
    observeBeacons()
    startLocationUpdates()
  }

  private fun observeBeacons() {
    viewModelScope.launch {
      beaconRepository.getAll().collect { beacons ->
        _beaconList.value = beacons // Ensure `beaconList` is updated
        _uiState.value = BeaconListUiState(beacons = beacons, loading = false)
      }
    }
  }

  private fun retrieveBeacons(location: Location1, context: Context) {
    viewModelScope.launch {
      // Update _uiState to reflect a loading state
      _uiState.value = BeaconListUiState(loading = true)

      createNearbyBeacons(
          location, _beaconList, 10000.0, context, beaconRepository, viewModelScope) {
            val updatedBeacons =
                _beaconList
                    .value // +  placeBeaconsRandomly(_beaconList.value, location, beaconRepository)
            // Update _uiState again once data is fetched
            _uiState.value = BeaconListUiState(beacons = updatedBeacons, loading = false)
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
    Log.d("MapViewModel", "Location: $location")
    return location?.let { LatLng(it.latitude, it.longitude) }
  }

  private fun startLocationUpdates() {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val locationRequest =
        LocationRequest.create().apply {
          interval = 600000 // Request location update every ten minute
          fastestInterval = 30000 // Accept updates as fast as every 30 seconds
          priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

    val locationCallback =
        object : LocationCallback() {
          override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            val location = locationResult.lastLocation
            location?.let {
              if (_areBeaconsLoaded.value) return
              retrieveBeacons(Location1(it.latitude, it.longitude), context)
              _areBeaconsLoaded.value = true
            }
          }
        }

    // Check permissions before requesting updates
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
      fusedLocationClient.requestLocationUpdates(
          locationRequest, locationCallback, Looper.getMainLooper())
    } else {
      Log.e("MapViewModel", "Location permission not granted")
    }
  }
}

data class BeaconListUiState(val beacons: List<Beacon> = listOf(), val loading: Boolean = false)
