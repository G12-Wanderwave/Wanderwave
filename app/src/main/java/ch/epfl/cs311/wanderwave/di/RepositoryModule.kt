package ch.epfl.cs311.wanderwave.di

import ch.epfl.cs311.wanderwave.model.localDb.AppDatabase
import ch.epfl.cs311.wanderwave.model.localDb.LocalAuthTokenRepository
import ch.epfl.cs311.wanderwave.model.repository.AuthTokenRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

  @Provides
  @Singleton
  fun provideAuthTokenRepository(appDatabase: AppDatabase): AuthTokenRepository {
    return LocalAuthTokenRepository(appDatabase)
  }
}
