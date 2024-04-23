package ch.epfl.cs311.wanderwave.model.repository

import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Track
import kotlinx.coroutines.flow.Flow

// BeaconRepository.kt
interface BeaconRepository {
  fun addItem(item: Beacon)

  fun addItemWithId(item: Beacon)

  fun updateItem(item: Beacon)

  fun deleteItem(item: Beacon)

  fun getItem(itemId: String): Flow<Beacon>

  fun getAll(): Flow<List<Beacon>>

  fun addTrackToBeacon(beaconId: String, track: Track, onComplete: (Boolean) -> Unit)
}
