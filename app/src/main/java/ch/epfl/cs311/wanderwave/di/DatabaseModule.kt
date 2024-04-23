package ch.epfl.cs311.wanderwave.di

import android.content.Context
import androidx.room.Room
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
        .allowMainThreadQueries()
        .build()
  }
}
