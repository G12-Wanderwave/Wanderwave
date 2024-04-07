package ch.epfl.cs311.wanderwave.model.repository

import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.localDb.LocalTrackRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Returns data from the first location available. For now, simply returns data from the local
 * database.
 */
class TrackRepositoryImpl @Inject constructor(private val localRepository: LocalTrackRepository) :
    TrackRepository {
  override fun getAll(): Flow<List<Track>> {
    return localRepository.getAll()
  }
}
