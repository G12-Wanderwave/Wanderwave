package ch.epfl.cs311.wanderwave.viewmodel

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfl.cs311.wanderwave.model.data.ListType
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.model.spotify.retrieveAndAddSubsectionFromSpotify
import ch.epfl.cs311.wanderwave.model.spotify.retrieveChildFromSpotify
import ch.epfl.cs311.wanderwave.viewmodel.interfaces.SpotifySongsActions
import com.spotify.protocol.types.ListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Define a simple class for a song list
data class SongList(val name: ListType, val tracks: List<Track> = mutableListOf())

@HiltViewModel
class ProfileViewModel
@Inject
constructor(
    private val repository: ProfileRepository,
    private val spotifyController: SpotifyController
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

  private val _isInEditMode = MutableStateFlow(false)
  val isInEditMode: StateFlow<Boolean> = _isInEditMode

  private val _isInPublicMode = MutableStateFlow(false)
  val isInPublicMode: StateFlow<Boolean> = _isInPublicMode

  // Add a state for managing song lists
  private val _songLists = MutableStateFlow<List<SongList>>(emptyList())
  val songLists: StateFlow<List<SongList>> = _songLists

  private var _spotifySubsectionList = MutableStateFlow<List<ListItem>>(emptyList())
  override val spotifySubsectionList: StateFlow<List<ListItem>> = _spotifySubsectionList

  private var _childrenPlaylistTrackList = MutableStateFlow<List<ListItem>>(emptyList())
  override val childrenPlaylistTrackList: StateFlow<List<ListItem>> = _childrenPlaylistTrackList

  private var _uiState = MutableStateFlow(ProfileViewModel.UIState())
  val uiState: StateFlow<ProfileViewModel.UIState> = _uiState

  fun createSpecificSongList(listType: ListType) {
    val listName = listType // Check if the list already exists
    val existingList = _songLists.value.firstOrNull { it.name == listName }
    if (existingList == null) {
      // Add new list if it doesn't exist
      _songLists.value = _songLists.value + SongList(listName)
    }
    // Do nothing if the list already exists
  }

  // Function to add a track to a song list
  override fun addTrackToList(listName: ListType, track: Track) {
    val updatedLists =
        _songLists.value.map { list ->
          if (list.name == listName) {
            if (list.tracks.contains(track)) return@map list

            list.copy(tracks = ArrayList(list.tracks).apply { add(track) })
          } else {
            list
          }
        }
    _songLists.value = updatedLists
  }

  fun updateProfile(updatedProfile: Profile) {
    _profile.value = updatedProfile
    viewModelScope.launch { repository.updateItem(updatedProfile) }
  }

  fun deleteProfile() {
    viewModelScope.launch { repository.deleteItem(_profile.value) }
  }

  fun togglePublicMode() {
    _isInPublicMode.value = !_isInPublicMode.value
  }

  fun getProfileByID(id: String) {
    viewModelScope.launch {
      repository.getItem(id).collect { fetchedProfile ->
        _uiState.value = UIState(profile = fetchedProfile, isLoading = false)
      }
    }
  }

  fun loadProfile(spotifyUid: String, snackbarHostState: SnackbarHostState, scope: CoroutineScope) {
    viewModelScope.launch {
      repository.isUidExisting(spotifyUid) { exists, profile ->
        if (exists && profile != null) {
          _profile.value = profile
        } else {
          // Show a snackbar message if the profile does not exist
          scope.launch { snackbarHostState.showSnackbar("Profile does not exist.") }
        }
      }
    }
  }

  /**
   * Get all the element of the main screen and add them to the top list
   *
   * @author Menzo Bouaissi
   * @since 2.0
   * @last update 3.0
   */
  override fun retrieveAndAddSubsection() {
    retrieveAndAddSubsectionFromSpotify(_spotifySubsectionList, spotifyController, viewModelScope)
  }
  /**
   * Get all the element of the main screen and add them to the top list
   *
   * @author Menzo Bouaissi
   * @since 2.0
   * @last update 3.0
   */
  override fun retrieveChild(item: ListItem) {
    retrieveChildFromSpotify(
        item, this._childrenPlaylistTrackList, spotifyController, viewModelScope)
  }

  data class UIState(
      val profile: Profile? = null,
      val isLoading: Boolean = true,
      val error: String? = null
  )
}
