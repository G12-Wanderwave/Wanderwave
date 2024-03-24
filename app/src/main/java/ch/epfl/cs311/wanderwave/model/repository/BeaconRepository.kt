package ch.epfl.cs311.wanderwave.model.repository

import ch.epfl.cs311.wanderwave.model.data.Beacon
import kotlinx.coroutines.flow.Flow

interface BeaconRepository {
  fun getAll(): Flow<List<Beacon>>
}