package ch.epfl.cs311.wanderwave.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
) : ViewModel() {

  private val _uiState = MutableStateFlow(UiState(loading = true))
  val uiState: StateFlow<UiState> = _uiState

  private var _searchQuery = MutableStateFlow("")

  init {
    observeTracks()
  }

  private fun observeTracks() {
    CoroutineScope(Dispatchers.IO).launch {
      repository.getAll().collect { tracks ->
        _uiState.value = UiState(tracks = tracks.filter { matchesSearchQuery(it) }, loading = false)
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
        CoroutineScope(Dispatchers.IO).launch {
          delay(300) // Debounce time in milliseconds
          _searchQuery.value = query
          observeTracks() // Re-filter tracks when search query changes
        }
  }

  /**
   * Selects the given track and updates the UI state accordingly.
   *
   * @param track The track to select.
   */
  fun selectTrack(track: Track) {
    spotifyController.playTrack(track)
  }

//  /**
//   * Plays the selected track if it's not already playing or resumes the paused track if it's the
//   * same as the selected track. If no track is selected, it updates the UI state with an
//   * appropriate message.
//   */
//  fun play() {
//    if (_uiState.value.selectedTrack != null && !_uiState.value.isPlaying) {
//
//      if (_uiState.value.pausedTrack == _uiState.value.selectedTrack) {
//        resumeTrack()
//      } else {
//        playTrack(_uiState.value.selectedTrack!!)
//      }
//
//      _uiState.value = _uiState.value.copy(isPlaying = true)
//    } else {
//      if (!_uiState.value.isPlaying) {
//        _uiState.value = _uiState.value.copy(message = "No track selected")
//      } else {
//        _uiState.value = _uiState.value.copy(message = "Track already playing")
//      }
//    }
//  }
//
  data class UiState(
      val tracks: List<Track> = listOf(),
      val loading: Boolean = false,
      val message: String? = null,
      val selectedTrack: Track? = null,
      val pausedTrack: Track? = null,
      val isPlaying: Boolean = false,
      val currentMillis: Int = 0,
      val expanded: Boolean = false,
      val progress: Float = 0f,
      val shuffleOn: Boolean = false,
      val repeatMode: RepeatMode = RepeatMode.NONE
  )
}

enum class RepeatMode {
  NONE,
  ONE,
  ALL
}
