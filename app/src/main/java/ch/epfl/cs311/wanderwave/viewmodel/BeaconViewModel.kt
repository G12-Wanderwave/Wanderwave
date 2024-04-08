package ch.epfl.cs311.wanderwave.viewmodel

import androidx.lifecycle.ViewModel
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class BeaconViewModel @Inject constructor(private val repository: ProfileRepositoryImpl) :
    ViewModel() {

  private val _beacon =
      MutableStateFlow(
          Beacon(
              id = "My Beacon ID", location = Location(0.0, 0.0, "My Location"), tracks = listOf()))

  val beacon: StateFlow<Beacon> = _beacon

  val beaconConnection = BeaconConnection()
}
