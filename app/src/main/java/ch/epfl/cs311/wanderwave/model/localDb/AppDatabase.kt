package ch.epfl.cs311.wanderwave.model.localDb

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TrackEntity::class, ProfileEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
  abstract fun trackDao(): TrackDao

  abstract fun profileDao(): ProfileDao
}
