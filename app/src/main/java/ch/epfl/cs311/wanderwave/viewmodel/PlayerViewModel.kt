package ch.epfl.cs311.wanderwave.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.ui.theme.spotify_green
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(val spotifyController: SpotifyController) : ViewModel() {
  private val _playerState = spotifyController.playerState()
  private val _expandedState = MutableStateFlow(false)

  private var _uiState = combine(_playerState, _expandedState) { playerState, expandedState ->
    Log.d("PlayerViewModel", "Flow updated")
    if (playerState == null) {
      UiState(expanded = expandedState)
    }
    else
    UiState(
      track = Track(
        /* TODO: I _think_ that the track uri includes "spotify:track:" at the beginning, so
        *   need to remove that. */
        playerState.track.uri,
        title = playerState.track.name,
        artist = playerState.track.artist.name,
      ),
      isPlaying = !playerState.isPaused,
      repeatMode = playerState.playbackOptions.repeatMode != RepeatMode.NONE.ordinal,
      isShuffling = playerState.playbackOptions.isShuffling,
      expanded = expandedState
    )
  }
  val uiState: StateFlow<UiState> = _uiState
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      UiState()
    )

  fun collapse() {
    _expandedState.value = false
  }

  fun expand() {
    Log.d("PlayerViewModel", "Emitting true works: ${_expandedState.tryEmit(true)}")
    Log.d("PlayerViewModel", "Expanded state: ${_expandedState.value}")
  }

  fun resume() {
    spotifyController.resumeTrack()
  }

  fun pause() {
    spotifyController.pauseTrack()
  }

  fun toggleShuffle() {
//    _uiState.value = _uiState.value.copy(shuffleOn = !_uiState.value.shuffleOn)
  }

  fun toggleRepeat() {
//    _uiState.value =
//      when (_uiState.value.repeatMode) {
//        RepeatMode.NONE -> _uiState.value.copy(repeatMode = RepeatMode.ALL)
//        RepeatMode.ALL -> _uiState.value.copy(repeatMode = RepeatMode.ONE)
//        else -> _uiState.value.copy(repeatMode = RepeatMode.NONE)
//      }
  }


  data class UiState(
    val track: Track? = null,
    val isPlaying: Boolean = false,
    val repeatMode: Boolean = false,
    val isShuffling: Boolean = false,
    val expanded: Boolean = false
  )
}
