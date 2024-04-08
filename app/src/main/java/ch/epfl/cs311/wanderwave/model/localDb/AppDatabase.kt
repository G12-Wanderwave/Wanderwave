package ch.epfl.cs311.wanderwave.model.localDb

import androidx.room.Database
import androidx.room.RoomDatabase
import ch.epfl.cs311.wanderwave.model.local.BeaconDao
import ch.epfl.cs311.wanderwave.model.local.BeaconEntity

@Database(entities = [TrackEntity::class, ProfileEntity::class, BeaconEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
  abstract fun trackDao(): TrackDao

  abstract fun profileDao(): ProfileDao

  abstract fun beaconDao(): BeaconDao
}
