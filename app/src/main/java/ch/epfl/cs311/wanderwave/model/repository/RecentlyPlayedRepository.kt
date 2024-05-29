package ch.epfl.cs311.wanderwave.model.repository

import ch.epfl.cs311.wanderwave.model.data.Track
import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface RecentlyPlayedRepository {

  fun addRecentlyPlayed(track: Track, timestamp: Instant)

  fun getRecentlyPlayed(): Flow<List<Track>>
}
