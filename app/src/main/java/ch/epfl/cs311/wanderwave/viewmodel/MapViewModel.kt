package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.ViewModel
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.android.gms.maps.GoogleMap

@HiltViewModel
class MapViewModel @Inject constructor(private val repository: BeaconRepositoryImpl) : ViewModel() {

  private val _uiState = MutableStateFlow(BeaconListUiState(loading = true))
  val uiState: StateFlow<BeaconListUiState> = _uiState

  init {
    observeBeacons()
  }

  private fun observeBeacons() {
    CoroutineScope(Dispatchers.IO).launch {
      repository.getAll().collect { beacons ->
        _uiState.value = BeaconListUiState(beacons = beacons, loading = false)
      }
    }
  }

  private var _googleMapState = MutableStateFlow<GoogleMap?>(null)
  val googleMapState: StateFlow<GoogleMap?> = _googleMapState

  /**
   * Set the GoogleMap to the given GoogleMap
   *
   * @author Menzo Bouaissi
   * @since 1.0
   * @last update 1.0
   */
  fun setGoogleMap(googleMap: GoogleMap) {
    _googleMapState.value = googleMap
  }
}

data class BeaconListUiState(val beacons: List<Beacon> = listOf(), val loading: Boolean = false)


