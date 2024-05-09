package ch.epfl.cs311.wanderwave.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ch.epfl.cs311.wanderwave.model.localDb.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

  @Provides
  @Singleton
  fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
    return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "app_database")
      .addMigrations(MIGRATION_1_2)
      .build()
  }

  // Migration from version 1 to version 2: adding the TrackRecord table
  val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("CREATE TABLE IF NOT EXISTS `track_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `beaconId` TEXT NOT NULL, `trackId` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
    }
  }
}
