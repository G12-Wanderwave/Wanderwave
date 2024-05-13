package ch.epfl.cs311.wanderwave.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull

// Define a simple class for a song list
data class SongList(val name: ListType, val tracks: List<Track> = mutableListOf())

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
              firstName = "...",
              lastName = "...",
              description = "...",
              numberOfLikes = 0,
              isPublic = true,
              spotifyUid = "",
              firebaseUid = "",
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
    repository.updateItem(updatedProfile)
  }

  fun deleteProfile() {
    repository.deleteItem(_profile.value)
  }

  fun togglePublicMode() {
    _isInPublicMode.value = !_isInPublicMode.value
  }

  suspend fun getProfileByID(id: String, create: Boolean = false) {
    repository.isUidExisting(id) { exists, fetchedProfile ->
      if (exists) {
        _profile.value = fetchedProfile!!
        _uiState.value = UIState(profile = fetchedProfile, isLoading = false)
      } else if (create) {
        val newProfile = profile.value.copy(firebaseUid = id)
        _profile.value = newProfile
        repository.addItemWithId(newProfile)
        _uiState.value = UIState(profile = newProfile, isLoading = false)
      } else {
        Log.e("ProfileViewModel", "Profile not found")
      }
    }
  }

  suspend fun getProfileOfCurrentUser(create: Boolean = false) {
    val currentUserId = authenticationController.getUserData()!!.id
    getProfileByID(currentUserId, create)
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
