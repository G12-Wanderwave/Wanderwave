package ch.epfl.cs311.wanderwave.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepository
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.model.spotify.getLikedTracksFromSpotify
import ch.epfl.cs311.wanderwave.model.spotify.getTotalLikedTracksFromSpotity
import ch.epfl.cs311.wanderwave.viewmodel.interfaces.SpotifySongsActions
import com.spotify.protocol.types.ListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class BeaconViewModel
@Inject
constructor(
    private val trackRepository: TrackRepository,
    private val beaconRepository: BeaconRepository,
    private val profileRepository: ProfileRepository,
    private val spotifyController: SpotifyController,
    private val authenticationController: AuthenticationController
) : ViewModel(), SpotifySongsActions {

  private var _uiState = MutableStateFlow(UIState())
  val uiState: StateFlow<UIState> = _uiState

  private val _likedSongsTrackList = MutableStateFlow<List<ListItem>>(emptyList())
  override val likedSongsTrackList: StateFlow<List<ListItem>> = _likedSongsTrackList

  private val _nbrLikedSongs = MutableStateFlow(0)
  override val nbrLikedSongs: StateFlow<Int> = _nbrLikedSongs
  var beaconId: String = ""

  fun getBeaconById(id: String) {
    viewModelScope.launch {
      beaconRepository.getItem(id).collect { fetchedBeacon ->
        // the fetched beacon has a result
        fetchedBeacon.onSuccess { beacon ->
          _uiState.value = uiState.value.copy(beacon = beacon, isLoading = false)
        }

        fetchedBeacon.onFailure { exception ->
          _uiState.value =
              uiState.value.copy(error = exception.message, isLoading = false, beacon = null)
          Log.e("BeaconViewModel", "Failed to get beacon by id: $id", exception)
        }
      }
    }

    viewModelScope.launch {
      val profileId = authenticationController.getUserData()!!.id
      profileRepository.getItem(profileId).collect { fetchedProfile ->
        fetchedProfile.onSuccess { profile ->
          _uiState.value = uiState.value.copy(bannedTracks = profile.bannedSongs)
        }
        fetchedProfile.onFailure { exception ->
          _uiState.value = uiState.value.copy(error = exception.message, isLoading = false)
          Log.e("BeaconViewModel", "Failed to get profile by id: $profileId", exception)
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

  // Function to add a track to a song list
  override fun addTrackToList(track: Track) {

    addTrackToBeacon(beaconId, track) { success ->
      Log.d("BeaconViewModel", "Track added to beacon")
      if (success) {
        getBeaconById(beaconId)
        Log.i("BeaconViewModel", "Track added to beacon")
      } else {
        Log.e("BeaconViewModel", "Failed to add track to beacon")
      }
    }
  }

  override suspend fun getLikedTracks(page: Int) {
    getLikedTracksFromSpotify(this._likedSongsTrackList, spotifyController, viewModelScope, page)
  }

  fun selectTrack(track: Track) {
    val tracks = uiState.value.beacon?.profileAndTrack?.map { it.track }
    tracks?.let { spotifyController.playTrackList(it, track) }
  }

  override suspend fun getTotalLikedTracks() {
    _nbrLikedSongs.value = getTotalLikedTracksFromSpotity(spotifyController)
  }

  override fun clearLikedSongs() {
    _likedSongsTrackList.value = emptyList()
  }

  data class UIState(
      val beacon: Beacon? = null,
      val bannedTracks: List<Track> = emptyList(),
      val isLoading: Boolean = true,
      val error: String? = null
  )
}
