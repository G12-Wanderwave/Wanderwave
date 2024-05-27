package ch.epfl.cs311.wanderwave.model.localDb

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import ch.epfl.cs311.wanderwave.model.data.TrackRecord

@Database(
    entities = [AuthTokenEntity::class, TrackRecord::class, RecentlyPlayedEntity::class],
    version = 3,
    autoMigrations = [AutoMigration(from = 2, to = 3)])
abstract class AppDatabase : RoomDatabase() {
  abstract fun authTokenDao(): AuthTokenDao

  abstract fun trackRecordDao(): TrackRecordDao // Provide access to TrackRecordDao

  abstract fun recentlyPlayedDao(): RecentlyPlayedDao
}
