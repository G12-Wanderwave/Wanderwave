package ch.epfl.cs311.wanderwave.model.localDb

import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.RecentlyPlayedRepository
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class LocalRecentlyPlayedRepository
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

      trackRepository.getItem(track.id).firstOrNull()?.onFailure {
        trackRepository.addItemWithId(track)
      }
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun getRecentlyPlayed(): Flow<List<Track>> {
    return recentlyPlayedDao
        .getRecentlyPlayed()
        .flatMapLatest { recentlyPlayed ->
          val flows: List<Flow<Result<Track>>> =
              recentlyPlayed.map { trackRepository.getItem(it.trackId) }
          combine(flows) { it.mapNotNull { it.getOrNull() }.toList() }
        }
        .flowOn(ioDispatcher)
  }
}
