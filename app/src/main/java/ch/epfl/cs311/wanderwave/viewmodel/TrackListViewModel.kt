package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.ViewModel
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class TrackListViewModel
@Inject
constructor(
    private val spotifyController: SpotifyController
) : ViewModel() {

  // TODO : implement the repository with the local database

  private val _uiState = MutableStateFlow(UiState(loading = true))
  val uiState: StateFlow<UiState> = _uiState

  init {
    observeTracks()
  }

  private fun observeTracks() {
    // CoroutineScope(Dispatchers.IO).launch {
    //   repository.getAll().collect { tracks ->
    //     _uiState.value = UiState(tracks = tracks, loading = false)
    //   }
    // }
  }

  fun playTrack(track: Track) {
    CoroutineScope(Dispatchers.IO).launch {
      if (!spotifyController.playTrack(track).first()) {
        _uiState.value = _uiState.value.copy(message = "Failed to play track")
      }
    }
  }

  fun collapse() {
    _uiState.value = _uiState.value.copy(expanded = false)
  }

  fun expand() {
    _uiState.value = _uiState.value.copy(expanded = true)
  }

  fun play() {
    _uiState.value = _uiState.value.copy(isPlaying = true)
  }

  fun pause() {
    _uiState.value = _uiState.value.copy(isPlaying = false, currentMillis = 1000)
  }

  data class UiState(
      val tracks: List<Track> = listOf(),
      val loading: Boolean = false,
      val message: String? = null,
      val selectedTrack: Track? = null,
      val isPlaying: Boolean = false,
      val currentMillis: Int = 0,
      val expanded: Boolean = false,
      val progress: Float = 0f
  )
}
