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
import ch.epfl.cs311.wanderwave.model.data.Location as Location1
import ch.epfl.cs311.wanderwave.model.data.Profile
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class MapViewModel
@Inject
constructor(
    val locationSource: LocationSource,
    private val beaconRepository: BeaconRepository,
    private val authenticationController: AuthenticationController,
    private val trackRepository: TrackRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {
  val cameraPosition = MutableLiveData<CameraPosition?>()

  private val _uiState = MutableStateFlow(BeaconListUiState(loading = true))
  val uiState: StateFlow<BeaconListUiState> = _uiState

  private var _beaconList = MutableStateFlow<List<Beacon>>(emptyList())
  val beaconList: StateFlow<List<Beacon>> = _beaconList

  private var _retrievedSong =
      MutableStateFlow<ProfileTrackAssociation>(ProfileTrackAssociation(null, Track("", "", "")))

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
              profilePictureUri = null))
  val profile: StateFlow<Profile> = _profile

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

  /**
   * Fetches the beacons in the vicinity of the given location
   * @param location The location to fetch the beacons from
   * @param context The context to use for fetching the beacons
   */
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

  /**
   * Fetches the beacons in the vicinity of the given location
   * @param context The context to use for fetching the beacons
   * @param location The location to fetch the beacons from
   */
  fun loadBeacons(context: Context, location: ch.epfl.cs311.wanderwave.model.data.Location) {

    if (!_areBeaconsLoaded.value) {
      retrieveBeacons(Location1(location.latitude, location.longitude), context)
      _areBeaconsLoaded.value = true
    }
  }

  /**
   * Fetches a random song from the list of songs associated with the given beacon id
   *
   * @param beaconId The id of the beacon to fetch the song from
   */
  fun getRandomSong(beaconId: String) {
    viewModelScope.launch {
      try {
        val fetchedBeacon = beaconRepository.getItem(beaconId).first()
        fetchedBeacon.onSuccess { beacon ->
          if (beacon.profileAndTrack.isNotEmpty()) {
            _retrievedSong.value = beacon.profileAndTrack.random()
            Log.i("Retrieved song", "Retrieved song: ${_retrievedSong.value.track}")
            Log.i("Beacon", "Beacon: $beacon")
            updateChosenSongsProfile()
          }
        }
        fetchedBeacon.onFailure { exception ->
          Log.e("No beacons found", "No beacons found for the given id $exception")
        }
      } catch (exception: Exception) {
        Log.e("Error", "Error fetching beacon: $exception")
      }
    }
  }

  /**
   * Retrieves a random song from the profile of the current user and adds it to the given beacon
   * @param beaconId The id of the beacon to add the song to
   */
  fun retrieveRandomSongFromProfileAndAddToBeacon(beaconId: String) {
    viewModelScope.launch {
      profile.value?.let { currentProfile ->
        try {
          val fetchedProfile = profileRepository.getItem(currentProfile.firebaseUid).first()

          Log.d("Profile", "Current profile: $fetchedProfile")
          Log.d("BeaconId", "BeaconId: $beaconId")
          fetchedProfile.onSuccess { profile ->
            if (profile.topSongs.isNotEmpty()) {
              val track = profile.topSongs.random()
              Log.d("Track", "Track: $track")
              val beacon = beaconRepository.getItem(beaconId).first()
              beacon.onSuccess {
                if (it.profileAndTrack.none { it.track.id == track.id }) {
                  val newProfileAndTrack =
                      it.profileAndTrack + ProfileTrackAssociation(profile, track)

                  val newBeacon = it.copy(profileAndTrack = newProfileAndTrack)
                  beaconRepository.updateItem(newBeacon)
                  _uiState.value =
                      _uiState.value.copy(
                          beacons =
                              _uiState.value.beacons.map { existingBeacon ->
                                if (existingBeacon.id == beaconId) newBeacon else existingBeacon
                              })
                }
              }
            }
          }

          fetchedProfile.onFailure { exception ->
            Log.e("Profile not found", "Profile not found for the given id: $exception")
          }
        } catch (exception: Exception) {
          Log.e("Error", "Error fetching profile: $exception")
        }
      }
    }
  }

  /**
   * Updates the profile of the current user by adding the chosen song to the list of chosen songs
   *
   */
  fun updateChosenSongsProfile() {
    viewModelScope.launch {
      val profileId = authenticationController.getUserData()?.id
      profileId?.let {
        try {
          val fetchedProfile = profileRepository.getItem(profileId).first()
          fetchedProfile.onSuccess { profile ->
            if (_retrievedSong.value.track.id.isNotEmpty() &&
                profile.chosenSongs.none { it.id == _retrievedSong.value.track.id }) {
              _profile.value =
                  profile.copy(
                      chosenSongs = profile.chosenSongs + listOf(_retrievedSong.value.track))
              profileRepository.updateItem(_profile.value)
            }
          }

          fetchedProfile.onFailure { exception ->
            Log.e("Profile not found", "Profile not found for the given id $exception")
          }
        } catch (exception: Exception) {}
      }
    }
  }

  /**
   * Fetches the profile of the current user
   */
  fun getProfileOfCurrentUser() {
    viewModelScope.launch {
      val currentUserId = authenticationController.getUserData()!!.id
      profileRepository.getItem(currentUserId).collect { fetchedProfile ->
        fetchedProfile.onSuccess { fetchedProfile -> _profile.value = fetchedProfile }
        fetchedProfile.onFailure { exception ->
          Log.e("Profile not found", "Profile not found for the given id $exception")
        }
      }
    }
  }
}

data class BeaconListUiState(val beacons: List<Beacon> = listOf(), val loading: Boolean = false)
