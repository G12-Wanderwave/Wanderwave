package ch.epfl.cs311.wanderwave.di

import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object AuthenticationModule {

  @Provides
  @Singleton
  fun provideAuthenticationController(httpClient: OkHttpClient): AuthenticationController {
    return AuthenticationController(Firebase.auth, httpClient)
  }
}
