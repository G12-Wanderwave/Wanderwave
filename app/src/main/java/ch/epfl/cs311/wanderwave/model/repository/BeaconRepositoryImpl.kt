package ch.epfl.cs311.wanderwave.model.repository

import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import kotlinx.coroutines.flow.Flow

// BeaconRepositoryImpl.kt
class BeaconRepositoryImpl(private val beaconConnection: BeaconConnection) : BeaconRepository {
  override fun addItem(item: Beacon) {
    beaconConnection.addItem(item)
  }

  override fun addItemWithId(item: Beacon) {
    beaconConnection.addItemWithId(item)
  }

  override fun updateItem(item: Beacon) {
    beaconConnection.updateItem(item)
  }

  override fun deleteItem(item: Beacon) {
    beaconConnection.deleteItem(item)
  }

  override fun getItem(itemId: String): Flow<Beacon> {
    return beaconConnection.getItem(itemId)
  }

  override fun getAll(): Flow<List<Beacon>> {
    return beaconConnection.getAll()
  }

  override fun addTrackToBeacon(beaconId: String, track: Track, onComplete: (Boolean) -> Unit) {
    beaconConnection.addTrackToBeacon(beaconId, track, onComplete)
  }
}
