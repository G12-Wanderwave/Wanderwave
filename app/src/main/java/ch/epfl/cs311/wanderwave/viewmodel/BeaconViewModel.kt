package ch.epfl.cs311.wanderwave.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

class BeaconViewModel @Inject constructor() : ViewModel() {
  private val beaconConnection = BeaconConnection()

  private var _uiState = MutableStateFlow<UIState>(UIState())
  val uiState: StateFlow<UIState> = _uiState

  fun getBeaconById(id: String) {
    viewModelScope.launch {
      beaconConnection.getItem(id).collect { fetchedBeacon ->
        _uiState.value = UIState(beacon = fetchedBeacon, isLoading = false)
      }
    }
  }

  data class UIState(
    val beacon: Beacon? = null,
    val isLoading: Boolean = true,
    val error: String? = null)
}
