package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepository
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

// Define a simple class for a song list

@HiltViewModel
class ProfileViewModel
@Inject
constructor(
    private val repository: ProfileRepository, // TODO revoir
    private val spotifyController: SpotifyController,
    private val authenticationController: AuthenticationController
) : ViewModel(), SpotifySongsActions {

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

  private val _isInPublicMode = MutableStateFlow(false)
  val isInPublicMode: StateFlow<Boolean> = _isInPublicMode

  // Add a state for managing song lists
  private val _songLists = MutableStateFlow(emptyList<Track>())
  val songLists: StateFlow<List<Track>> = _songLists

  private var _uiState = MutableStateFlow(UIState())
  val uiState: StateFlow<UIState> = _uiState

    private val _likedSongsTrackList = MutableStateFlow<List<ListItem>>(emptyList())
    override val likedSongsTrackList: StateFlow<List<ListItem>> = _likedSongsTrackList

    private val _nbrLikedSongs = MutableStateFlow(0)
    override val nbrLikedSongs: StateFlow<Int> = _nbrLikedSongs

  // Function to add a track to a song list
  override fun addTrackToList(track: Track) {
    val newTrack =
        if (!track.id.contains("spotify:track:")) {
          Track("spotify:track:" + track.id, track.title, track.artist)
        } else {
          track
        }
    if (!_songLists.value.contains(newTrack)) _songLists.value += mutableListOf(newTrack)
  }

  fun updateProfile(updatedProfile: Profile) {
    _profile.value = updatedProfile
    repository.updateItem(updatedProfile)
  }

  fun deleteProfile() {
    repository.deleteItem(_profile.value)
  }

  fun togglePublicMode() {
    _isInPublicMode.value = !_isInPublicMode.value
  }

  fun selectTrack(track: Track) {
    spotifyController.playTrack(track)
  }

  fun getProfileByID(id: String, create: Boolean) {
    viewModelScope.launch {
      repository.getItem(id).collect { fetchedProfile ->
        fetchedProfile.onSuccess { profile ->
          _profile.value = profile
          _uiState.value = UIState(profile = profile, isLoading = false)
        }
        fetchedProfile.onFailure { exception ->
          if (exception.message == "Document does not exist" && create) {
            val newProfile = profile.value.copy(firebaseUid = id)
            repository.addItemWithId(newProfile)
            _uiState.value = UIState(error = "Creating Profile...", isLoading = true)
          } else {
            _uiState.value = UIState(error = exception.message, isLoading = false)
          }
        }
      }
    }
  }
    override fun clearLikedSongs() {
        _likedSongsTrackList.value = emptyList()
    }
  fun getProfileOfCurrentUser(create: Boolean) {
    val currentUserId = authenticationController.getUserData()!!.id
    getProfileByID(currentUserId, create)
  }

  /**
   * Get all the liked tracks of the user and add them to the likedSongs list.
   *
   * @author Menzo Bouaissi
   * @since 3.0
   * @last update 3.0
   */
  override suspend fun getLikedTracks(page:Int) {
    getLikedTracksFromSpotify(this._likedSongsTrackList, spotifyController, viewModelScope,page)
  }

    /**
     * Get the total number of liked tracks of the user.
     */
override suspend fun getTotalLikedTracks() {
    _nbrLikedSongs.value = getTotalLikedTracksFromSpotity(spotifyController)
  }
  data class UIState(
      val profile: Profile? = null,
      val isLoading: Boolean = true,
      val error: String? = null
  )
}
