package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A ViewModel that holds the state of the GoogleMap.
 *
 * @author Menzo Bouaissi
 * @since 1.0
 * @last update 1.0
 */
@HiltViewModel
class MapViewModel @Inject constructor() : ViewModel() {
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
