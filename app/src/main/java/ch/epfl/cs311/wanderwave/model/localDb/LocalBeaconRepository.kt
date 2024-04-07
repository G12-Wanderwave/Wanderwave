package ch.epfl.cs311.wanderwave.model.local

import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.localDb.AppDatabase
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LocalBeaconRepository @Inject constructor(private val database: AppDatabase)
  : BeaconRepository {

  private val beaconDao = database.beaconDao()

  private fun BeaconEntity.toBeacon() = Beacon(id, Location(latitude, longitude))

  override fun getAll(): Flow<List<Beacon>> {
    return flow { emit(beaconDao.getAll().map { it.toBeacon() }) }
  }
}