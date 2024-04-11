package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.CameraPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(val locationSource: LocationSource) : ViewModel() {
  val cameraPosition = MutableLiveData<CameraPosition?>()
}
