package ch.epfl.cs311.wanderwave.model.local

import androidx.room.Dao
import androidx.room.Query

@Dao
interface TrackDao {
  @Query("SELECT * FROM tracks") fun getAll(): List<TrackEntity>
}
