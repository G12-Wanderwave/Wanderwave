package ch.epfl.cs311.wanderwave.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfl.cs311.wanderwave.model.data.ListType
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.localDb.AppDatabase
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.model.spotify.getLikedTracksFromSpotify
import ch.epfl.cs311.wanderwave.model.spotify.getTracksFromSpotifyPlaylist
import ch.epfl.cs311.wanderwave.model.spotify.retrieveAndAddSubsectionFromSpotify
import ch.epfl.cs311.wanderwave.model.spotify.retrieveChildFromSpotify
import ch.epfl.cs311.wanderwave.viewmodel.interfaces.SpotifySongsActions
import com.spotify.protocol.types.ListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@HiltViewModel
class TrackListViewModel
@Inject
constructor(
    private val spotifyController: SpotifyController,
    private val appDatabase: AppDatabase,
    private val repository: TrackRepository
) : ViewModel(), SpotifySongsActions {

  private val _uiState = MutableStateFlow(UiState(loading = true))
  val uiState: StateFlow<UiState> = _uiState

  private val _isTopSongsListVisible = MutableStateFlow(true)
  override val isTopSongsListVisible: StateFlow<Boolean> = _isTopSongsListVisible

  private var _searchQuery = MutableStateFlow("")

  fun loadTracksBasedOnSource(index: Int) {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(loading = true)
      when (index) {
        0 ->
            _uiState.value =
                _uiState.value.copy(tracks = spotifyController.recentlyPlayedTracks.value)
        1 -> loadRecentlyAddedTracks()
        2 -> loadRecentlyAddedTracks() // TODO: modify here for the banned tracks
      }
    }
  }

  fun loadRecentlyAddedTracks() {
    viewModelScope.launch {
      val trackRecords =
          appDatabase.trackRecordDao().getAllRecentlyAddedTracks().firstOrNull() ?: listOf()
      val trackDetails =
          trackRecords.mapNotNull {
            repository.getItem(it.trackId).firstOrNull()?.getOrElse { null }
          }
      _uiState.value = _uiState.value.copy(tracks = trackDetails, loading = false)
    }
  }

  private var _spotifySubsectionList = MutableStateFlow<List<ListItem>>(emptyList())
  override val spotifySubsectionList: StateFlow<List<ListItem>> = _spotifySubsectionList

  private var _childrenPlaylistTrackList = MutableStateFlow<List<ListItem>>(emptyList())
  override val childrenPlaylistTrackList: StateFlow<List<ListItem>> = _childrenPlaylistTrackList

  private val _likedSongsTrackList = MutableStateFlow<List<ListItem>>(emptyList())
  override val likedSongsTrackList: StateFlow<List<ListItem>> = _likedSongsTrackList

  init {
    observeTracks()
  }

  private fun observeTracks() {
    viewModelScope.launch {
      repository.getAll().collect { tracks ->
        _uiState.value =
            UiState(
                tracks = tracks.filter { matchesSearchQuery(it) },
                loading = false,
                progress = spotifyController.trackProgress)
      }
      // deal with the flow
    }
  }

  private fun matchesSearchQuery(track: Track): Boolean {
    return track.title.contains(_searchQuery.value, ignoreCase = true) ||
        track.artist.contains(_searchQuery.value, ignoreCase = true)
  }

  private var searchJob: Job? = null

  fun setSearchQuery(query: String) {
    searchJob?.cancel()
    searchJob =
        viewModelScope.launch {
          delay(300) // Debounce time in milliseconds
          _searchQuery.value = query
          observeTracks() // Re-filter tracks when search query changes
        }
  }

  override fun addTrackToList(listName: ListType, track: Track) {
    Log.d("ProfileViewModel", "addTrackToList $track")
    val newTrack =
        if (!track.id.contains("spotify:track:")) {
          Track("spotify:track:" + track.id, track.title, track.artist)
        } else {
          track
        }
    Log.d("ProfileViewModel", "addTrackToListnewTrack $newTrack")

    val updatedTracks = _uiState.value.tracks + newTrack
    _uiState.value = _uiState.value.copy(tracks = updatedTracks)
    _childrenPlaylistTrackList.value = (emptyList())
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
    retrieveChildFromSpotify(item, _childrenPlaylistTrackList, spotifyController, viewModelScope)
  }

  /**
   * Plays the given track using the SpotifyController.
   *
   * @param track The track to play.
   */
  fun playTrack(track: Track) {
    spotifyController.playTrackList(uiState.value.tracks, track)
  }

  fun collapse() {
    _uiState.value = _uiState.value.copy(expanded = false)
  }

  fun expand() {
    _uiState.value = _uiState.value.copy(expanded = true)
  }

  override suspend fun getLikedTracks() {
    getLikedTracksFromSpotify(this._likedSongsTrackList, spotifyController, viewModelScope)
  }

  override fun getTracksFromPlaylist(playlistId: String) {
    getTracksFromSpotifyPlaylist(
        playlistId, _childrenPlaylistTrackList, spotifyController, viewModelScope)
  }

  override fun emptyChildrenList() {
    _childrenPlaylistTrackList.value = (emptyList())
  }

  data class UiState(
      val tracks: List<Track> = listOf(),
      val loading: Boolean = false,
      val expanded: Boolean = false,
      val progress: MutableFloatState = mutableFloatStateOf(0f),
      val isShuffled: Boolean = false,
      val loopMode: SpotifyController.RepeatMode = SpotifyController.RepeatMode.OFF
  )
}
