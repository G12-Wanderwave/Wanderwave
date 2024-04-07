package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor() : ViewModel() {
    private var _googleMapState = MutableStateFlow<GoogleMap?>(null)
    val googleMapState: StateFlow<GoogleMap?> = _googleMapState

    fun setGoogleMap(googleMap: GoogleMap) {
        _googleMapState.value = googleMap
    }
}