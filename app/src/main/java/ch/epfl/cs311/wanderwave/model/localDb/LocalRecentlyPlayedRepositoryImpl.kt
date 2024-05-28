package ch.epfl.cs311.wanderwave.model.localDb

import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.RecentlyPlayedRepository
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
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

      trackRepository.getItem(track.id).firstOrNull()?.onFailure {
        trackRepository.addItemWithId(track)
      }
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun getRecentlyPlayed(): Flow<List<Track>> {
    return recentlyPlayedDao.getRecentlyPlayed().flatMapLatest { recentlyPlayed ->
      val flows: List<Flow<Result<Track>>> = recentlyPlayed.map { trackRepository.getItem(it.trackId) }
      Log.d("LocalRecentlyPlayedRepositoryImpl", "getRecentlyPlayed: $flows")
      val result: Flow<List<Track>> = combine(flows) {
        Log.d("LocalRecentlyPlayedRepositoryImpl", "combine: $it")
        it.mapNotNull { it.getOrNull() }.toList()
      }
      result
    }.flowOn(ioDispatcher)
  }
}
