package ch.epfl.cs311.wanderwave.model.localDb

import androidx.room.Database
import androidx.room.RoomDatabase
import ch.epfl.cs311.wanderwave.model.data.TrackRecord

@Database(entities = [AuthTokenEntity::class, TrackRecord::class], version = 2) // Note the addition of TrackRecord and increment in version number
abstract class AppDatabase : RoomDatabase() {
  abstract fun authTokenDao(): AuthTokenDao
  abstract fun trackRecordDao(): TrackRecordDao  // Provide access to TrackRecordDao
}
