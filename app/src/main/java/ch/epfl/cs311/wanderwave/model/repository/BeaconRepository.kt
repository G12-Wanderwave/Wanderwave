package ch.epfl.cs311.wanderwave.model.repository

import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Track
import kotlinx.coroutines.flow.Flow

// BeaconRepository.kt
interface BeaconRepository : FirebaseRepository<Beacon> {

  fun getAll(): Flow<List<Beacon>>

  fun addTrackToBeacon(
      beaconId: String,
      track: Track,
      profileUid: String,
      onComplete: (Boolean) -> Unit
  )

  suspend fun addItemAndGetId(item: Beacon): String?
}
