package ch.epfl.cs311.wanderwave.di

import android.content.Context
import ch.epfl.cs311.wanderwave.model.localDb.AppDatabase
import ch.epfl.cs311.wanderwave.model.localDb.LocalAuthTokenRepository
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import ch.epfl.cs311.wanderwave.model.remote.ProfileConnection
import ch.epfl.cs311.wanderwave.model.remote.TrackConnection
import ch.epfl.cs311.wanderwave.model.repository.AuthTokenRepository
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepository
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

  @Provides
  @Singleton
  fun provideBeaconRepository(
    @ApplicationContext context: Context,
    trackRepository: TrackRepository,
    profileRepository: ProfileRepository
  ): BeaconRepository {
    return BeaconConnection(
      trackConnection = trackRepository as TrackConnection,
      profileConnection = profileRepository as ProfileConnection,
      ioDispatcher = Dispatchers.IO)
  }

  @Provides
  @Singleton
  fun provideTrackRepository(@ApplicationContext context: Context): TrackRepository {
    return TrackConnection()
  }

  @Provides
  @Singleton
  fun provideProfileRepository(
    @ApplicationContext context: Context,
    trackRepository: TrackRepository
  ): ProfileRepository {
    return ProfileConnection(trackConnection = trackRepository as TrackConnection)
  }

  @Provides
  @Singleton
  fun provideAuthTokenRepository(appDatabase: AppDatabase): AuthTokenRepository {
    return LocalAuthTokenRepository(appDatabase, Dispatchers.IO)
  }
}
