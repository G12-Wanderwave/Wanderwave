package ch.epfl.cs311.wanderwave.model.repository

import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.TrackConnection
import kotlinx.coroutines.flow.Flow

// TrackRepositoryImpl.kt
class TrackRepositoryImpl(private val trackConnection: TrackConnection) : TrackRepository {
  override fun addItem(track: Track) {
    trackConnection.addItem(track)
  }

  override fun updateItem(track: Track) {
    trackConnection.updateItem(track)
  }

  override fun deleteItem(track: Track) {
    trackConnection.deleteItem(track)
  }

  override fun getItem(trackId: String): Flow<Track> {
    return trackConnection.getItem(trackId)
  }

  override fun getAllItems(): Flow<List<Track>> {
    return trackConnection.getAll()
  }

  override fun addItemsIfNotExist(tracks: List<Track>) {
    trackConnection.addItemsIfNotExist(tracks)
  }

  override fun getAll(): Flow<List<Track>> {
    return trackConnection.getAll()
  }
}
