package ch.epfl.cs311.wanderwave.model.repository

import ch.epfl.cs311.wanderwave.model.data.Track
import kotlinx.coroutines.flow.Flow

// TrackRepository.kt
interface TrackRepository : FirebaseRepository<Track> {
  fun addItemsIfNotExist(tracks: List<Track>)

  fun getAll(): Flow<List<Track>>

  // Method to fetch a single track by its ID
  fun getTrackById(trackId: String): Flow<Track?>
}
