package ch.epfl.cs311.wanderwave.di

import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.repository.AuthTokenRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object AuthenticationModule {

  @Provides
  @Singleton
  fun provideAuthenticationController(
      httpClient: OkHttpClient,
      authenticationRepository: AuthTokenRepository
  ): AuthenticationController {
    return AuthenticationController(
        Firebase.auth, httpClient, authenticationRepository, Dispatchers.IO)
  }
}
