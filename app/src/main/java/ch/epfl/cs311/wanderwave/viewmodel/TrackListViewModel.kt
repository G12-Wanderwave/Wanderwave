package ch.epfl.cs311.wanderwave.viewmodel

import android.annotation.SuppressLint
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
import kotlinx.coroutines.flow.first
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

  @SuppressLint("ExperimentalAnnotationRetention")
  @RequiresOptIn(message = "This part of the API is visible only for testing.")
  internal annotation class ForTestingPurposesOnly

  @ForTestingPurposesOnly
  internal fun testUpdateUiState(newState: UiState) {
      _uiState.value = newState
  }
  private fun observeTracks() {
    CoroutineScope(Dispatchers.IO).launch {
      repository.getAll().collect { tracks ->
        _uiState.value = UiState(tracks = tracks, loading = false)
      }
    }
  }

fun playTrack(track: Track) {
    CoroutineScope(Dispatchers.IO).launch {
        if (!spotifyController.playTrack(track).first()) {
            _uiState.value = _uiState.value.copy(message = "Failed to play track")
        }
    }
}

fun resumeTrack() {
    CoroutineScope(Dispatchers.IO).launch {
        if (!spotifyController.resumeTrack().first()) {
            _uiState.value = _uiState.value.copy(message = "Failed to resume track")
        }
    }
}
fun pauseTrack() {
    CoroutineScope(Dispatchers.IO).launch {
        if (!spotifyController.pauseTrack().first()) {
            _uiState.value = _uiState.value.copy(message = "Failed to pause track")
        }
    }
}

fun selectTrack(track: Track) {
        _uiState.value = _uiState.value.copy(selectedTrack = track)

}
  fun collapse() {
    _uiState.value = _uiState.value.copy(expanded = false)
  }

  fun expand() {
    _uiState.value = _uiState.value.copy(expanded = true)
  }

  fun play() {
      //TODO remove
      val t = Track("spotify:track:6ImuyUQYhJKEKFtlrstHCD","Main Title", "John Williams")
      _uiState.value = _uiState.value.copy(selectedTrack = t)
      if(_uiState.value.selectedTrack != null && !_uiState.value.isPlaying){

          if(_uiState.value.pausedTrack == _uiState.value.selectedTrack){
              resumeTrack()
          }
          else {
              playTrack(_uiState.value.selectedTrack!!)
          }

          _uiState.value = _uiState.value.copy(isPlaying = true)
      } else {
          if(!_uiState.value.isPlaying){
              _uiState.value = _uiState.value.copy(message = "No track selected")
          }else{
              _uiState.value = _uiState.value.copy(message = "Track already playing")
          }
      }
  }

  fun pause() {
      if(_uiState.value.isPlaying) {
          pauseTrack()
          _uiState.value = _uiState.value.copy(isPlaying = false, currentMillis = 1000,
                                               pausedTrack = _uiState.value.selectedTrack)
      } else {
          _uiState.value = _uiState.value.copy(message = "No track playing")
      }
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
