package ch.epfl.cs311.wanderwave.di

import android.content.Context
import ch.epfl.cs311.wanderwave.model.localDb.AppDatabase
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import ch.epfl.cs311.wanderwave.model.remote.ProfileConnection
import ch.epfl.cs311.wanderwave.model.remote.TrackConnection
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepository
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConnectionModule {

    @Provides
    @Singleton
    fun provideBeaconRepository(
        @ApplicationContext context: Context,
        appDatabase: AppDatabase  // Inject AppDatabase here
    ): BeaconRepository {
        return BeaconConnection(
            trackConnection = TrackConnection(),
            profileConnection = ProfileConnection(),
            appDatabase = appDatabase,  // Use the injected AppDatabase
            ioDispatcher = Dispatchers.IO
        )
    }

    @Provides
    @Singleton
    fun provideTrackRepository(@ApplicationContext context: Context): TrackRepository {
        return TrackConnection()
    }

    @Provides
    @Singleton
    fun provideProfileRepository(@ApplicationContext context: Context): ProfileRepository {
        return ProfileConnection()
    }
}
