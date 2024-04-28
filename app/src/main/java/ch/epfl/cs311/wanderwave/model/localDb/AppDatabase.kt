package ch.epfl.cs311.wanderwave.model.localDb

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AuthTokenEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun authTokenDao(): AuthTokenDao
}