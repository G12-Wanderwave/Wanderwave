package ch.epfl.cs311.wanderwave.model.repository

import ch.epfl.cs311.wanderwave.model.data.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {

  fun getAll(): Flow<List<Track>>

  fun getTrackById(id: String): Flow<Track>
}
