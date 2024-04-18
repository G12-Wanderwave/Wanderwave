package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.ViewModel
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.TrackRepositoryImpl
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@HiltViewModel
class TrackListViewModel
@Inject
constructor(
    private val repository: TrackRepositoryImpl,
    private val spotifyController: SpotifyController
) : ViewModel() {

  private val _uiState = MutableStateFlow(UiState(loading = true))
  val uiState: StateFlow<UiState> = _uiState

  init {
    observeTracks()
  }

  private fun observeTracks() {
    CoroutineScope(Dispatchers.IO).launch {
      repository.getAll().collect { tracks ->
        _uiState.value = UiState(tracks = tracks, loading = false)
      }
    }
  }

  private fun playTrack(track: Track) {
    CoroutineScope(Dispatchers.IO).launch {
      val success = spotifyController.playTrack(track).firstOrNull()
      if (success == null || !success) {
        _uiState.value = _uiState.value.copy(message = "Failed to play track")
      }
    }
  }

  private fun resumeTrack() {
    CoroutineScope(Dispatchers.IO).launch {
      val success = spotifyController.resumeTrack().firstOrNull()
      if (success == null || !success) {
        _uiState.value = _uiState.value.copy(message = "Failed to resume track")
      }
    }
  }

  private fun pauseTrack() {
    CoroutineScope(Dispatchers.IO).launch {
      val success = spotifyController.pauseTrack().firstOrNull()
      if (success == null || !success) {
        _uiState.value = _uiState.value.copy(message = "Failed to pause track")
      }
    }
  }

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

  private fun skip(dir: Int) {
    if (_uiState.value.selectedTrack != null && (dir == 1 || dir == -1)) {
      _uiState.value.tracks.indexOf(_uiState.value.selectedTrack).let { it: Int ->
        val next = Math.floorMod((it + dir), _uiState.value.tracks.size)
        selectTrack(_uiState.value.tracks[next])
      }
    }
  }

  fun skipForward() {
    skip(1)
  }

  fun skipBackward() {
    skip(-1)
  }

  data class UiState(
      val tracks: List<Track> = listOf(),
      val loading: Boolean = false,
      val message: String? = null,
      val selectedTrack: Track? = null,
      val pausedTrack: Track? = null,
      val isPlaying: Boolean = false,
      val currentMillis: Int = 0,
      val expanded: Boolean = false,
      val progress: Float = 0f
  )
}
