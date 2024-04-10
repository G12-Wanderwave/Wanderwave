package ch.epfl.cs311.wanderwave.di

import android.content.Context
import ch.epfl.cs311.wanderwave.model.location.FastLocationSource
import com.google.android.gms.maps.LocationSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MapModule {

  @Provides
  @Singleton
  fun provideMapController(@ApplicationContext context: Context): LocationSource {
    return FastLocationSource(context)
  }
}