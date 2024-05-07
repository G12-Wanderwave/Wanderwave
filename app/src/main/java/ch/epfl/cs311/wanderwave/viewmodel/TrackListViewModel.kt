package ch.epfl.cs311.wanderwave.viewmodel

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
import kotlinx.coroutines.flow.firstOrNull
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
    spotifyController.setOnTrackEndCallback { skipForward() }
  }

  private fun observeTracks() {
    viewModelScope.launch {
      repository.getAll().collect { tracks ->
        _uiState.value =
            UiState(
                tracks = tracks.filter { matchesSearchQuery(it) },
                queue = tracks.filter { matchesSearchQuery(it) },
                loading = false)
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
    _uiState.value.tracks += track
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
  private fun playTrack(track: Track) {
    viewModelScope.launch {
      val success = spotifyController.playTrack(track).firstOrNull()
      if (success == null || !success) {
        _uiState.value = _uiState.value.copy(message = "Failed to play track")
      }
    }
  }

  /** Resumes the currently paused track using the SpotifyController. */
  private fun resumeTrack() {
    viewModelScope.launch {
      val success = spotifyController.resumeTrack().firstOrNull()
      if (success == null || !success) {
        _uiState.value = _uiState.value.copy(message = "Failed to resume track")
      }
    }
  }

  /** Pauses the currently playing track using the SpotifyController. */
  private fun pauseTrack() {
    viewModelScope.launch {
      val success = spotifyController.pauseTrack().firstOrNull()
      if (success == null || !success) {
        _uiState.value = _uiState.value.copy(message = "Failed to pause track")
      }
    }
  }

  /**
   * Selects the given track and updates the UI state accordingly.
   *
   * @param track The track to select.
   */
  fun selectTrack(track: Track) {
    _uiState.value = _uiState.value.copy(selectedTrack = track)
    _uiState.value = _uiState.value.copy(pausedTrack = null)
    if (_uiState.value.isPlaying) playTrack(track)
  }

  fun collapse() {
    _uiState.value = _uiState.value.copy(expanded = false)
  }

  fun expand() {
    _uiState.value = _uiState.value.copy(expanded = true)
  }

  /**
   * Plays the selected track if it's not already playing or resumes the paused track if it's the
   * same as the selected track. If no track is selected, it updates the UI state with an
   * appropriate message.
   */
  fun play() {
    if (_uiState.value.selectedTrack != null && !_uiState.value.isPlaying) {

      if (_uiState.value.pausedTrack == _uiState.value.selectedTrack) {
        resumeTrack()
      } else {
        playTrack(_uiState.value.selectedTrack!!)
      }

      _uiState.value = _uiState.value.copy(isPlaying = true)
    } else {
      if (!_uiState.value.isPlaying) {
        _uiState.value = _uiState.value.copy(message = "No track selected")
      } else {
        _uiState.value = _uiState.value.copy(message = "Track already playing")
      }
    }
  }

  /**
   * Pauses the currently playing track and updates the UI state accordingly. If no track is
   * playing, it updates the UI state with an appropriate message.
   */
  fun pause() {
    if (_uiState.value.isPlaying) {
      pauseTrack()
      _uiState.value =
          _uiState.value.copy(
              isPlaying = false, currentMillis = 1000, pausedTrack = _uiState.value.selectedTrack)
    } else {
      _uiState.value = _uiState.value.copy(message = "No track playing")
    }
  }

  /**
   * Skips to the next or previous track in the list.
   *
   * @param dir The direction to skip in. 1 for next, -1 for previous.
   */
  private fun skip(dir: Int) {
    if (_uiState.value.selectedTrack != null && (dir == 1 || dir == -1)) {
      _uiState.value.queue.indexOf(_uiState.value.selectedTrack).let { it: Int ->
        var next = it + dir
        when (_uiState.value.loopMode) {
          LoopMode.ONE -> next = it
          LoopMode.ALL -> next = Math.floorMod((it + dir), _uiState.value.queue.size)
          else -> {
            /** Do nothing */
          }
        }
        if (next >= 0 && next < _uiState.value.queue.size) {
          selectTrack(_uiState.value.queue[next])
        } else {
          pause()
          _uiState.value = _uiState.value.copy(selectedTrack = null)
        }
      }
    }
  }

  /** Skips to the next track in the list. */
  fun skipForward() {
    skip(1)
  }

  /** Skips to the previous track in the list. */
  fun skipBackward() {
    skip(-1)
  }

  /** Toggles the shuffle state of the queue. */
  fun toggleShuffle() {
    if (_uiState.value.isShuffled) {
      _uiState.value = _uiState.value.copy(queue = _uiState.value.tracks, isShuffled = false)
    } else {
      _uiState.value =
          _uiState.value.copy(queue = _uiState.value.tracks.shuffled(), isShuffled = true)
    }
  }

  /** Toggles the looping state of the player. */
  fun toggleLoop() {
    _uiState.value =
        when (_uiState.value.loopMode) {
          LoopMode.NONE -> _uiState.value.copy(loopMode = LoopMode.ALL)
          LoopMode.ALL -> _uiState.value.copy(loopMode = LoopMode.ONE)
          else -> _uiState.value.copy(loopMode = LoopMode.NONE)
        }
  }

  /** Sets the looping state of the player. */
  fun setLoop(loopMode: LoopMode) {
    _uiState.value = _uiState.value.copy(loopMode = loopMode)
  }

  data class UiState(
      var tracks: List<Track> = listOf(),
      val queue: List<Track> = listOf(),
      val loading: Boolean = false,
      val message: String? = null,
      val selectedTrack: Track? = null,
      val pausedTrack: Track? = null,
      val isPlaying: Boolean = false,
      val currentMillis: Int = 0,
      val expanded: Boolean = false,
      val progress: Float = 0f,
      val isShuffled: Boolean = false,
      val loopMode: LoopMode = LoopMode.NONE
  )
}

enum class LoopMode {
  NONE,
  ONE,
  ALL
}
