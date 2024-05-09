package ch.epfl.cs311.wanderwave.model.localDb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ch.epfl.cs311.wanderwave.model.data.TrackRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackRecordDao {
  @Insert
  suspend fun insertTrackRecord(trackRecord: TrackRecord) // Method to insert a new track record

  @Query("SELECT * FROM track_records ORDER BY timestamp DESC")
  fun getAllRecentlyAddedTracks(): Flow<List<TrackRecord>>
}
