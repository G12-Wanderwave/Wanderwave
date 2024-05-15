package ch.epfl.cs311.wanderwave.viewmodel

import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfl.cs311.wanderwave.model.data.ListType
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
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
import kotlinx.coroutines.launch

@HiltViewModel
class TrackListViewModel
@Inject
constructor(
    private val spotifyController: SpotifyController,
    private val repository: TrackRepository
) : ViewModel(), SpotifySongsActions {

  private val _uiState = MutableStateFlow(UiState(loading = true))
  val uiState: StateFlow<UiState> = _uiState

  private var _searchQuery = MutableStateFlow("")

  private var _spotifySubsectionList = MutableStateFlow<List<ListItem>>(emptyList())
  override val spotifySubsectionList: StateFlow<List<ListItem>> = _spotifySubsectionList

  private var _childrenPlaylistTrackList = MutableStateFlow<List<ListItem>>(emptyList())
  override val childrenPlaylistTrackList: StateFlow<List<ListItem>> = _childrenPlaylistTrackList

  init {
    observeTracks()
  }

  private fun observeTracks() {
    viewModelScope.launch {
      repository.getAll().collect { tracks ->
        _uiState.value = UiState(tracks = tracks.filter { matchesSearchQuery(it) }, loading = false, progress = spotifyController.trackProgress)
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
    val updatedTracks = _uiState.value.tracks + track
    _uiState.value = _uiState.value.copy(tracks = updatedTracks)
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

  data class UiState(
      val tracks: List<Track> = listOf(),
      val loading: Boolean = false,
      val expanded: Boolean = false,
      val progress: MutableFloatState = mutableFloatStateOf(0f),
  )
}

enum class LoopMode {
  NONE,
  ONE,
  ALL
}
