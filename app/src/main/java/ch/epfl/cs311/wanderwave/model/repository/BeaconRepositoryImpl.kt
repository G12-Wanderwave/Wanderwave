package ch.epfl.cs311.wanderwave.model.repository

import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.local.LocalBeaconRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BeaconRepositoryImpl @Inject constructor(private val localRepository: LocalBeaconRepository)
  : BeaconRepository {

  override fun getAll(): Flow<List<Beacon>> {
    return localRepository.getAll()
  }
}