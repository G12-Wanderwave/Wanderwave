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
  val beaconConnection = BeaconConnection()

  private val _beacon = MutableStateFlow<Beacon?>(null)
  val beacon: StateFlow<Beacon?> = _beacon

  fun getBeaconById(id: String) {
    viewModelScope.launch {
      beaconConnection.getItem(id).collect { fetchedBeacon ->
        _beacon.value = fetchedBeacon
      }
    }
  }

}
