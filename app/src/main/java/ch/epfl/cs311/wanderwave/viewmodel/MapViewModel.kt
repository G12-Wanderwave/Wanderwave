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
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.Location as Location1
import ch.epfl.cs311.wanderwave.model.data.ProfileTrackAssociation
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepository
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
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
constructor(val locationSource: LocationSource,
            private val beaconRepository: BeaconRepository,
            private val authenticationController: AuthenticationController,
            private val trackRepository: TrackRepository,
            private val profileRepository: ProfileRepository) :
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

  private val _profile =
    MutableStateFlow(
      Profile(
        firstName = "My FirstName",
        lastName = "My LastName",
        description = "My Description",
        numberOfLikes = 0,
        isPublic = true,
        spotifyUid = "My Spotify UID",
        firebaseUid = "My Firebase UID",
        profilePictureUri = null)
    )
  val profile: StateFlow<Profile> = _profile
  private var _profileUI = MutableStateFlow(ProfileViewModel.UIState())
  private val profileUI: StateFlow<ProfileViewModel.UIState> = _profileUI
  init {
    observeBeacons()
    //getProfileOfCurrentUser()
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

  /**
   * Fetches a random song from the list of songs associated with the given beacon id
   *
   * @param id The id of the beacon
   * @author Menzo Bouaissi
   * @since 4.0
   */
  fun getRandomSongAndAddToProfile(beaconId: String) {
    viewModelScope.launch {
      beaconRepository.getItem(beaconId).collect { fetchedBeacon ->
        fetchedBeacon.onSuccess { beacon ->
          if (beacon.profileAndTrack.isEmpty()) {
            Log.e("No songs found", "No songs found for the given id")
            return@onSuccess
          }
          _retrievedSongs.value = beacon.profileAndTrack.random()
          trackRepository.addItemsIfNotExist(listOf(_retrievedSongs.value.track))
        }
        fetchedBeacon.onFailure { exception ->
          Log.e("No beacons found", "No beacons found for the given id")
        }
      }
    }
  }

  fun retrieveSongFromProfileAndAddToBeacon(beaconId: String) {

    Log.d("ProfileViewModel", "addTrackToListnewTrack ")
    viewModelScope.launch {
      Log.d("ProfileViewModel", "addTrackToListnewTrack ${_profileUI.value}")

      _profileUI.value.profile?.let {
        Log.d("ProfileViewModel", "addTrackToListnewTrack ${_profileUI.value}")
        profileRepository.getItem(it.firebaseUid).collect { fetchedProfile ->
          fetchedProfile.onSuccess { profile ->
            if (profile.topSongs.isEmpty()) {
              Log.e("No songs found", "No songs found for the given id")
              return@onSuccess
            }
            addTrackToBeacon(beaconId,  profile.topSongs.random()) { success ->
              if (success) {
                Log.i("BeaconViewModel", "Track added to beacon")
              } else {
                Log.e("BeaconViewModel", "Failed to add track to beacon")
              }
            }
          }
          fetchedProfile.onFailure { exception ->
            Log.e("No profile found", "No profile found for the given id")
          }
        }
      }
    }
  }

  fun addTrackToBeacon(beaconId: String, track: Track, onComplete: (Boolean) -> Unit) {
    // Call the BeaconConnection's addTrackToBeacon with the provided beaconId and track
    val correctTrack = track.copy(id = "spotify:track:" + track.id)
    trackRepository.addItemsIfNotExist(listOf(correctTrack))
    beaconRepository.addTrackToBeacon(
        beaconId, correctTrack, authenticationController.getUserData()!!.id, onComplete)
  }



  fun getProfileOfCurrentUser() {

    val currentUserId = authenticationController.getUserData()!!.id
    Log.d("ProfileViewModel", "getProfileOfCurrentUser $currentUserId")
    viewModelScope.launch {
      profileRepository.getItem(currentUserId).collect { fetchedProfile ->
        fetchedProfile.onSuccess { profile ->
          _profile.value = profile
          _profileUI.value = ProfileViewModel.UIState(profile = profile, isLoading = false)
          Log.d("ProfileViewModel", "getProfileOfCurrentUser $profile")
        }
        fetchedProfile.onFailure { exception ->
          Log.e("Profile not found", "Profile not found for the given id $exception")
        }
      }
    }
  }

}


data class BeaconListUiState(val beacons: List<Beacon> = listOf(), val loading: Boolean = false)
