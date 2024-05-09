package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
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
) : ViewModel() {

  private val _uiState = MutableStateFlow(UiState(loading = true))
  val uiState: StateFlow<UiState> = _uiState

  private var _searchQuery = MutableStateFlow("")

  init {
    observeTracks()
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

  /**
   * Plays the given track using the SpotifyController.
   *
   * @param track The track to play.
   */
  fun playTrack(track: Track) {
    spotifyController.playTrackList(uiState.value.tracks, track)
  }

  /** Resumes the currently paused track using the SpotifyController. */
  fun resumeTrack() {
    spotifyController.resumeTrack {}
  }

  /** Pauses the currently playing track using the SpotifyController. */
  fun pauseTrack() {
    spotifyController.pauseTrack {}
  }

  fun collapse() {
    _uiState.value = _uiState.value.copy(expanded = false)
  }

  fun expand() {
    _uiState.value = _uiState.value.copy(expanded = true)
  }

  data class UiState(
      val tracks: List<Track> = listOf(),
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
