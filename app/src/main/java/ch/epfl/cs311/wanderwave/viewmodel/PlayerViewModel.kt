package ch.epfl.cs311.wanderwave.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.model.spotify.toWanderwaveTrack
import dagger.hilt.android.lifecycle.HiltViewModel
import hilt_aggregated_deps._ch_epfl_cs311_wanderwave_viewmodel_LoginScreenViewModel_HiltModules_BindsModule
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PlayerViewModel @Inject constructor(val spotifyController: SpotifyController) : ViewModel() {
  private val _playerState = spotifyController.playerState()
  private val _looping = spotifyController.looping
  private val _shuffling = spotifyController.shuffling
  private val _expandedState = MutableStateFlow(false)

  private var _uiState =
      combine(_playerState, _expandedState, _looping, _shuffling) { playerState, expandedState, looping, shuffling ->
        if (playerState == null) {
          UiState(expanded = expandedState)
        } else
            UiState(
                track = playerState.track?.toWanderwaveTrack(),
                isPlaying = !playerState.isPaused,
                repeatMode = looping,
                isShuffling = shuffling,
                expanded = expandedState)
      }
  val uiState: StateFlow<UiState> =
      _uiState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

  init {
    spotifyController.setOnTrackEndCallback { skipForward() }
  }

  fun collapse() {
    _expandedState.value = false
  }

  fun expand() {
    _expandedState.value = true
  }

  fun resume() {
    spotifyController.resumeTrack()
  }

  fun pause() {
    spotifyController.pauseTrack()
  }

  fun skipForward() {
    viewModelScope.launch {
      spotifyController.skip(1, {}, {})
      Log.d("PlayerViewModel", "skipForward")
    }
  }

  fun skipBackward() {
    viewModelScope.launch { spotifyController.skip(-1) }
  }

  fun toggleShuffle() {
    _shuffling.value = !_shuffling.value
  }

  fun toggleRepeat() {
    _looping.value = !_looping.value
  }

  data class UiState(
      val track: Track? = null,
      val isPlaying: Boolean = false,
      val repeatMode: Boolean = false,
      val isShuffling: Boolean = false,
      val expanded: Boolean = false
  )
}
