package ch.epfl.cs311.wanderwave.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.ProfileTrackAssociation
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.model.spotify.getLikedTracksFromSpotify
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
    private val spotifyController: SpotifyController,
    private val authenticationController: AuthenticationController
) : ViewModel(), SpotifySongsActions {

  private var _uiState = MutableStateFlow(UIState())
  val uiState: StateFlow<UIState> = _uiState

  private val _likedSongsTrackList = MutableStateFlow<List<ListItem>>(emptyList())
  override val likedSongsTrackList: StateFlow<List<ListItem>> = _likedSongsTrackList

  var beaconId: String = ""

  init {
    val sampleBeacon =
        Beacon(
            id = "Sample ID",
            location = Location(0.0, 0.0, "Sample Location"),
            profileAndTrack =
                listOf(
                    ProfileTrackAssociation(
                        Profile(
                            "Sample First Name",
                            "Sample last name",
                            "Sample desc",
                            0,
                            false,
                            null,
                            "Sample Profile ID",
                            "Sample Track ID"),
                        Track("Sample Track ID", "Sample Track Title", "Sample Artist Name"))))

    _uiState.value = UIState(beacon = sampleBeacon, isLoading = false)
  }

  fun getBeaconById(id: String) {
    viewModelScope.launch {
      beaconRepository.getItem(id).collect { fetchedBeacon ->
        // the fetched beacon has a result
        fetchedBeacon.onSuccess { beacon ->
          _uiState.value = UIState(beacon = beacon, isLoading = false)
        }

        fetchedBeacon.onFailure { exception ->
          _uiState.value = UIState(error = exception.message, isLoading = false, beacon = null)
          Log.e("BeaconViewModel", "Failed to get beacon by id: $id", exception)
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
      if (success) {
        getBeaconById(beaconId)
        Log.i("BeaconViewModel", "Track added to beacon")
      } else {
        Log.e("BeaconViewModel", "Failed to add track to beacon")
      }
    }
  }

  override suspend fun getLikedTracks() {
    getLikedTracksFromSpotify(this._likedSongsTrackList, spotifyController, viewModelScope)
  }

  fun selectTrack(track: Track) {
    val tracks = uiState.value.beacon?.profileAndTrack?.map { it.track }
    tracks?.let { spotifyController.playTrackList(it, track) }
  }

  data class UIState(
      val beacon: Beacon? = null,
      val isLoading: Boolean = true,
      val error: String? = null
  )
}
