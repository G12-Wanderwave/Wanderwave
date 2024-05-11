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
import com.google.firebase.firestore.FirebaseFirestore
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
  fun provideFirestore(@ApplicationContext context: Context): FirebaseFirestore {
    return FirebaseFirestore.getInstance()
  }

  @Provides
  @Singleton
  fun provideBeaconRepository(
      @ApplicationContext context: Context,
      db: FirebaseFirestore,
      trackRepository: TrackRepository,
      profileRepository: ProfileRepository
  ): BeaconRepository {
    return BeaconConnection(
        database = db,
        trackConnection = trackRepository as TrackConnection,
        profileConnection = profileRepository as ProfileConnection,
        ioDispatcher = Dispatchers.IO)
  }

  @Provides
  @Singleton
  fun provideTrackRepository(
      @ApplicationContext context: Context,
      db: FirebaseFirestore
  ): TrackRepository {
    return TrackConnection(database = db)
  }

  @Provides
  @Singleton
  fun provideProfileRepository(
      @ApplicationContext context: Context,
      db: FirebaseFirestore,
      trackRepository: TrackRepository
  ): ProfileRepository {
    return ProfileConnection(database = db, trackConnection = trackRepository as TrackConnection)
  }

  @Provides
  @Singleton
  fun provideAuthTokenRepository(appDatabase: AppDatabase): AuthTokenRepository {
    return LocalAuthTokenRepository(appDatabase, Dispatchers.IO)
  }
}
