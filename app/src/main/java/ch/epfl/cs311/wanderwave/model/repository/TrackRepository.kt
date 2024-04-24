package ch.epfl.cs311.wanderwave.model.repository

import ch.epfl.cs311.wanderwave.model.data.Track
import kotlinx.coroutines.flow.Flow

// TrackRepository.kt
interface TrackRepository : FirebaseRepository<Track> {
  fun addItemsIfNotExist(tracks: List<Track>)

  fun getAll(): Flow<List<Track>>
}
