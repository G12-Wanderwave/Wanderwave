package ch.epfl.cs311.wanderwave.model.local

import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LocalTrackRepository @Inject constructor(private val database: AppDatabase) :
    TrackRepository {

  private val trackDao = database.trackDao()

  private fun TrackEntity.toTrack() = Track(id, title, artist)

  override fun getAll(): Flow<List<Track>> {
    return flow { emit(trackDao.getAll().map { it.toTrack() }) }
  }
}
