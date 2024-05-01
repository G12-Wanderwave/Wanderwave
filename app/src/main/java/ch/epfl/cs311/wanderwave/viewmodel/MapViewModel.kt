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
import ch.epfl.cs311.wanderwave.model.data.Track
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
constructor(
    val locationSource: LocationSource,
    private val beaconRepository: BeaconRepository,
    profileViewModel: ProfileViewModel
) : ViewModel() {
  val cameraPosition = MutableLiveData<CameraPosition?>()

  private val _uiState = MutableStateFlow(BeaconListUiState(loading = true))
  val uiState: StateFlow<BeaconListUiState> = _uiState

  // TODO: Define global constants
  private val BEACON_RANGE = 0.025 // 25 meters

  init {
    observeBeacons()
    val track = profileViewModel.songLists.value[0].tracks[0]
    isInBeaconRange(track)
  }

  private fun observeBeacons() {
    viewModelScope.launch {
      beaconRepository.getAll().collect { beacons ->
        _uiState.value = BeaconListUiState(beacons = beacons, loading = false)
      }
    }
  }

  /**
   * Check if the user is in range of a beacon
   *
   * @param track The track to drop
   */
  private fun isInBeaconRange(track: Track) {
    locationSource.activate { location ->
      // Convert the location to a Location object
      val loc = ch.epfl.cs311.wanderwave.model.data.Location(location.latitude, location.longitude)

      // Get the closest beacon
      val closestBeacon =
          _uiState.value.beacons.withIndex().minBy { (_, b) -> b.location.distanceBetween(loc) }

      // Check if the closest beacon is within range
      if (closestBeacon.value.location.distanceBetween(loc) < BEACON_RANGE) {
        dropTrack(closestBeacon.value, track)
      }
    }
  }

  /**
   * Add a track to a beacon
   *
   * @param beacon The beacon to add the track to
   * @param track The track to add
   */
  private fun dropTrack(beacon: Beacon, track: Track) {
    beacon.addTrack(track)
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
