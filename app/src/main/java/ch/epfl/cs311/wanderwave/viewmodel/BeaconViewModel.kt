package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class BeaconViewModel @Inject constructor() : ViewModel() {
  private val beaconConnection = BeaconConnection()
  private val id = "UAn8OUadgrUOKYagf8a2"

  private var _uiState = MutableStateFlow(UIState())
  val uiState: StateFlow<UIState> = _uiState

  init {
    getBeaconById(id)
  }

  fun getBeaconById(id: String) {
    viewModelScope.launch {
      beaconConnection.getItem(id).collect { fetchedBeacon ->
        _uiState.value = UIState(beacon = fetchedBeacon, isLoading = false)
      }
    }
  }

  fun addTrackToBeacon(beaconId: String, track: Track, onComplete: (Boolean) -> Unit) {
    // Call the BeaconConnection's addTrackToBeacon with the provided beaconId and track
    beaconConnection.addTrackToBeacon(beaconId, track, onComplete)
  }

  data class UIState(
      val beacon: Beacon? = null,
      val isLoading: Boolean = true,
      val error: String? = null
  )
}
