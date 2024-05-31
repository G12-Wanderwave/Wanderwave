package ch.epfl.cs311.wanderwave.di

import android.content.Context
import androidx.room.Room
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.localDb.AppDatabase
import ch.epfl.cs311.wanderwave.model.location.FastLocationSource
import ch.epfl.cs311.wanderwave.model.repository.AuthTokenRepository
import ch.epfl.cs311.wanderwave.model.repository.RecentlyPlayedRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import com.google.android.gms.maps.LocationSource
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

  @Provides
  @Singleton
  fun provideAuthenticationController(
      httpClient: OkHttpClient,
      authenticationRepository: AuthTokenRepository
  ): AuthenticationController {
    return AuthenticationController(
        Firebase.auth, httpClient, authenticationRepository, Dispatchers.IO)
  }

  @Provides
  @Singleton
  fun provideSpotifyController(
      @ApplicationContext context: Context,
      authenticationController: AuthenticationController,
      recentlyPlayedRepository: RecentlyPlayedRepository
  ): SpotifyController {
    return SpotifyController(
        context, authenticationController, Dispatchers.IO, recentlyPlayedRepository)
  }

  @Provides
  @Singleton
  fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
    return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "app_database")
        .fallbackToDestructiveMigration()
        .build()
  }

  @Provides
  @Singleton
  fun provideLocationSource(@ApplicationContext context: Context): LocationSource {
    return FastLocationSource(context)
  }

  @Provides
  @Singleton
  fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient()
  }
}
