package ch.epfl.cs311.wanderwave.model.localDb

import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.RecentlyPlayedRepository
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocalRecentlyPlayedRepositoryImpl
@Inject
constructor(
    private val recentlyPlayedDao: RecentlyPlayedDao,
    private val trackRepository: TrackRepository,
    private val ioDispatcher: CoroutineDispatcher
) : RecentlyPlayedRepository {
  override fun addRecentlyPlayed(track: Track, timestamp: Instant) {
    CoroutineScope(ioDispatcher).launch {
      val entity = RecentlyPlayedEntity(track.id, timestamp.epochSecond)
      recentlyPlayedDao.insertRecentlyPlayed(entity)

      if (trackRepository.getItem(track.id).firstOrNull() == null) {
        trackRepository.addItem(track)
      }
    }
  }

  override fun getRecentlyPlayed(): Flow<List<Track>> {
    return flow {
      withContext(ioDispatcher) {
        recentlyPlayedDao.getRecentlyPlayed().collect { recentlyPlayed ->
          val flows =
              recentlyPlayed.map {
                trackRepository.getItem(it.trackId).mapNotNull { it.getOrNull() }
              }

          combine(flows) { tracks -> emit(tracks.toList()) }
        }
      }
    }
  }
}
