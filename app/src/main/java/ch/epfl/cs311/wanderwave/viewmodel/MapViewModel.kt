package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.LocationSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(val locationSource: LocationSource) : ViewModel() {

}