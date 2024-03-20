package ch.epfl.cs311.wanderwave.model.local

import androidx.room.Dao
import androidx.room.Query
import ch.epfl.cs311.wanderwave.model.data.Track

@Dao
interface TrackDao {
  @Query("SELECT * FROM tracks") fun getAll(): List<Track>

  @Query("SELECT * FROM tracks WHERE id = :id") fun getTrackById(id: String): Track
}
