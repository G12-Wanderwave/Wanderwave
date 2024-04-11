package ch.epfl.cs311.wanderwave.model.local

import androidx.room.Dao
import androidx.room.Query

@Dao
interface BeaconDao {
  @Query("SELECT * FROM beacons") fun getAll(): List<BeaconEntity>
}
