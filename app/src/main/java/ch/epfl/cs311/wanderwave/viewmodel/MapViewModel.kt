package ch.epfl.cs311.wanderwave.viewmodel

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location as Location1
import ch.epfl.cs311.wanderwave.model.data.ProfileTrackAssociation
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import ch.epfl.cs311.wanderwave.model.utils.createNearbyBeacons
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

  private var _beaconList = MutableStateFlow<List<Beacon>>(emptyList())
  val beaconList: StateFlow<List<Beacon>> = _beaconList

  private var _retrievedSongs =
      MutableStateFlow<ProfileTrackAssociation>(ProfileTrackAssociation(null, Track("", "", "")))
  val retrievedSongs: StateFlow<ProfileTrackAssociation> = _retrievedSongs

  private val _areBeaconsLoaded = MutableStateFlow(false)

  init {
    observeBeacons()
  }

  private fun observeBeacons() {
    viewModelScope.launch {
      beaconRepository.getAll().collect { beacons ->
        _beaconList.value = beacons // Ensure `beaconList` is updated
        _uiState.value = _uiState.value.copy(beacons = beacons, loading = false)
      }
    }
  }

  private fun retrieveBeacons(location: Location1, context: Context) {
    viewModelScope.launch {
      // Update _uiState to reflect a loading state
      _uiState.value = _uiState.value.copy(loading = true)

      createNearbyBeacons(
          location, _beaconList, 10000.0, context, beaconRepository, viewModelScope) {
            val updatedBeacons = _beaconList.value
            // Update _uiState again once data is fetched
            _uiState.value = _uiState.value.copy(beacons = updatedBeacons, loading = false)
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

  fun loadBeacons(context: Context, location: ch.epfl.cs311.wanderwave.model.data.Location) {

    if (!_areBeaconsLoaded.value) {
      retrieveBeacons(Location1(location.latitude, location.longitude), context)
      _areBeaconsLoaded.value = true
    }
  }

  fun getRandomSong(id: String) {
    viewModelScope.launch {
      beaconRepository.getItem(id).collect { fetchedBeacon ->
        fetchedBeacon.onSuccess { beacon ->
          _retrievedSongs.value = beacon.profileAndTrack.random()
        }
        fetchedBeacon.onFailure { exception ->
          Log.e("No beacons found", "No beacons found for the given id")
        }
      }
    }
  }
}

data class BeaconListUiState(val beacons: List<Beacon> = listOf(), val loading: Boolean = false)
