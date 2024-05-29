package ch.epfl.cs311.wanderwave.model.localDb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentlyPlayedDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRecentlyPlayed(recentlyPlayed: RecentlyPlayedEntity)

  @Query("SELECT * FROM recently_played ORDER BY last_played DESC LIMIT 25")
  fun getRecentlyPlayed(): Flow<List<RecentlyPlayedEntity>>
}
