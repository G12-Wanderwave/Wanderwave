package ch.epfl.cs311.wanderwave.di

import android.content.Context
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import ch.epfl.cs311.wanderwave.model.remote.ProfileConnection
import ch.epfl.cs311.wanderwave.model.remote.TrackConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConnectionModule {

  @Provides
  @Singleton
  fun provideBeaconConnection(@ApplicationContext context: Context): BeaconConnection {
    return BeaconConnection()
  }

  @Provides
  @Singleton
  fun provideTrackConnection(@ApplicationContext context: Context): TrackConnection {
    return TrackConnection()
  }

  @Provides
  @Singleton
  fun provideProfileConnection(@ApplicationContext context: Context): ProfileConnection {
    return ProfileConnection()
  }
}
