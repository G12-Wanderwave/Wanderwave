package ch.epfl.cs311.wanderwave.model.repository

import ch.epfl.cs311.wanderwave.model.data.Track
import kotlinx.coroutines.flow.Flow

// TrackRepository.kt
interface TrackRepository {
  fun addItem(track: Track)

  fun updateItem(track: Track)

  fun deleteItem(track: Track)

  fun getItem(trackId: String): Flow<Track>

  fun getAllItems(): Flow<List<Track>>

  fun addItemsIfNotExist(tracks: List<Track>)

  fun getAll(): Flow<List<Track>>
}
