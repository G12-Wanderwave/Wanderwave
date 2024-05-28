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
import ch.epfl.cs311.wanderwave.model.spotify.getLikedTracksFromSpotify
import ch.epfl.cs311.wanderwave.model.spotify.getTracksFromSpotifyPlaylist
import ch.epfl.cs311.wanderwave.model.spotify.retrieveAndAddSubsectionFromSpotify
import ch.epfl.cs311.wanderwave.model.spotify.retrieveChildFromSpotify
import ch.epfl.cs311.wanderwave.viewmodel.interfaces.SpotifySongsActions
import com.spotify.protocol.types.ListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

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
              firstName = "My FirstName",
              lastName = "My LastName",
              description = "My Description",
              numberOfLikes = 0,
              isPublic = true,
              spotifyUid = "My Spotify UID",
              firebaseUid = "My Firebase UID",
              profilePictureUri = null))
  val profile: StateFlow<Profile> = _profile

  private val _isTopSongsListVisible = MutableStateFlow(true)
  override val isTopSongsListVisible: StateFlow<Boolean> = _isTopSongsListVisible

  private val _isInPublicMode = MutableStateFlow(false)
  val isInPublicMode: StateFlow<Boolean> = _isInPublicMode

  // Add a state for managing song lists
  private val _songLists = MutableStateFlow<List<SongList>>(emptyList())
  val songLists: StateFlow<List<SongList>> = _songLists

  private var _spotifySubsectionList = MutableStateFlow<List<ListItem>>(emptyList())
  override val spotifySubsectionList: StateFlow<List<ListItem>> = _spotifySubsectionList

  private var _childrenPlaylistTrackList = MutableStateFlow<List<ListItem>>(emptyList())
  override val childrenPlaylistTrackList: StateFlow<List<ListItem>> = _childrenPlaylistTrackList

  private var _uiState = MutableStateFlow(UIState())
  val uiState: StateFlow<UIState> = _uiState

  private val _likedSongsTrackList = MutableStateFlow<List<ListItem>>(emptyList())
  override val likedSongsTrackList: StateFlow<List<ListItem>> = _likedSongsTrackList

  private val _wanderwaveLikedTracks = MutableStateFlow<List<Track>>(emptyList())
  val wanderwaveLikedTracks: StateFlow<List<Track>> = _wanderwaveLikedTracks

  fun createSpecificSongList(listType: ListType) {
    val listName = listType // Check if the list already exists
    val existingList = _songLists.value.firstOrNull { it.name == listName }
    if (existingList == null) {
      // Add new list if it doesn't exist
      _songLists.value += SongList(listName)
    }
    // Do nothing if the list already exists
  }

  // Function to add a track to a song list
  override fun addTrackToList(listName: ListType, track: Track) {
    Log.d("ProfileViewModel", "addTrackToList $track")
    val newTrack =
        if (!track.id.contains("spotify:track:")) {
          Track("spotify:track:" + track.id, track.title, track.artist)
        } else {
          track
        }
    Log.d("ProfileViewModel", "addTrackToListnewTrack $newTrack")

    val updatedLists =
        _songLists.value.map { list ->
          if (list.name == listName) {

            if (list.tracks.contains(newTrack)) return@map list

            list.copy(tracks = ArrayList(list.tracks).apply { add(newTrack) })
          } else {
            list
          }
        }
    _songLists.value = updatedLists
    _childrenPlaylistTrackList.value = (emptyList())
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

  /**
   * Get all the element of the main screen and add them to the top list
   *
   * @author Menzo Bouaissi
   * @since 2.0
   * @last update 2.0
   */
  fun retrieveTracksFromSpotify() {
    viewModelScope.launch {
      val track = spotifyController.getAllElementFromSpotify().firstOrNull()
      if (track != null) {
        for (i in track) {
          if (i.hasChildren) {
            val children = spotifyController.getAllChildren(i).firstOrNull()
            if (children != null) {
              for (child in children) {
                addTrackToList(ListType.TOP_SONGS, Track(child.id, child.title, child.subtitle))
              }
            }
          }
        }
      }
    }
  }

  fun selectTrack(track: Track, listName: String) {
    val trackList = _songLists.value.firstOrNull { it.name.name == listName }
    if (trackList != null) {
      spotifyController.playTrackList(trackList = trackList.tracks, track)
    } else {
      spotifyController.playTrack(track)
    }
  }

  fun changeChosenSongs() {
    _isTopSongsListVisible.value = !_isTopSongsListVisible.value
  }

  fun getProfileByID(id: String, create: Boolean) {
    viewModelScope.launch {
      repository.getItem(id).collect { fetchedProfile ->
        fetchedProfile.onSuccess { profile ->
          profile.profilePictureUri = null // TODO : @Clarence, pls can you do it more properly
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

  suspend fun getProfileOfCurrentUser(create: Boolean) {
    val currentUserId = authenticationController.getUserData()!!.id
    getProfileByID(currentUserId, create)
  }

  override fun getTracksFromPlaylist(playlistId: String) {
    viewModelScope.launch {
      getTracksFromSpotifyPlaylist(
          playlistId, _childrenPlaylistTrackList, spotifyController, viewModelScope)
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
    _spotifySubsectionList.value = emptyList()
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
    _childrenPlaylistTrackList.value = emptyList()
    retrieveChildFromSpotify(item, _childrenPlaylistTrackList, spotifyController, viewModelScope)
  }

  /**
   * Get all the liked tracks of the user and add them to the likedSongs list.
   *
   * @author Menzo Bouaissi
   * @since 3.0
   * @last update 3.0
   */
  override suspend fun getLikedTracks() {
    getLikedTracksFromSpotify(this._likedSongsTrackList, spotifyController, viewModelScope)
  }

  override fun emptyChildrenList() {
    _childrenPlaylistTrackList.value = (emptyList())
  }

  /**
   * Like a song and add it to the liked songs list.
   *
   * @param track the track to like
   */
  suspend fun likeTrack(track: Track) {
    // Check if song is already liked
    if (!wanderwaveLikedTracks.value.contains(track)) _wanderwaveLikedTracks.value += track
      Log.d("ProfileViewModel", "likeTrack")
      spotifyController.addToPlaylist(track)
  }

  /**
   * Unlike a song and remove it from the liked songs list.
   *
   * @param track the track to unlike
   */
  fun unlikeTrack(track: Track) {
    // Check if song was not liked
    if (wanderwaveLikedTracks.value.contains(track)) _wanderwaveLikedTracks.value -= track
    viewModelScope.launch {
      Log.d("ProfileViewModel", "unlikeTrack")
      spotifyController.removeFromPlaylist(track)
    }
  }

  data class UIState(
      val profile: Profile? = null,
      val isLoading: Boolean = true,
      val error: String? = null
  )
}
