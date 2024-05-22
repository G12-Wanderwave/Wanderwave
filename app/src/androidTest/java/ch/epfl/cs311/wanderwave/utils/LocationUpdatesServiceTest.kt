package ch.epfl.cs311.wanderwave.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.model.utils.LocationUpdatesService
import com.google.android.gms.location.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocationUpdatesServiceTest {
  @Test
  fun test1() = runBlocking {
    // Initialize context properly
    val context: Context = mockk(relaxed = true)
    val service = spyk(LocationUpdatesService(context))

    // Mock necessary components
    val fusedLocationProviderClient: FusedLocationProviderClient = mockk(relaxed = true)
    val locationCallbackSlot = slot<LocationCallback>()
    val notificationManager: NotificationManager = mockk(relaxed = true)
    val localBroadcastManager: LocalBroadcastManager = mockk(relaxed = true)

    // Mock static methods correctly
    mockkStatic(ActivityCompat::class)
    mockkStatic(LocalBroadcastManager::class)

    every { LocalBroadcastManager.getInstance(any()) } returns localBroadcastManager
    every {
      fusedLocationProviderClient.requestLocationUpdates(
          any(), capture(locationCallbackSlot), any())
    } returns mockk()

    // Use the context to get the system service
    every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager
    every { context.applicationContext } returns context
    every { context.packageName } returns "ch.epfl.cs311.wanderwave"

    // Mock permission checks
    every {
      ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
    } returns PackageManager.PERMISSION_GRANTED
    every {
      ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
    } returns PackageManager.PERMISSION_GRANTED

    // Handle NotificationChannel creation for Oreo and above
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      every { notificationManager.createNotificationChannel(any<NotificationChannel>()) } just Runs
    }

    // Use dependency injection for FusedLocationProviderClient
    mockkConstructor(FusedLocationProviderClient::class)
    every {
      anyConstructed<FusedLocationProviderClient>()
          .requestLocationUpdates(any(), capture(locationCallbackSlot), any())
    } returns mockk()

    // Call onCreate to trigger the logic
    service.onCreate()

    // Verify interactions
    verify { service.startForegroundService() }
  }

  //
  //    @Test
  //    fun test2() = runBlocking {
  //        val context: Context = ApplicationProvider.getApplicationContext() // Use real context
  //        val localBroadcastManager: LocalBroadcastManager = mockk(relaxed = true)
  //
  //        mockkStatic(LocalBroadcastManager::class)
  //        every { LocalBroadcastManager.getInstance(any()) } returns localBroadcastManager
  //
  //        val service = spyk(LocationUpdatesService(context))
  //        val locationCallbackSlot = slot<LocationCallback>()
  //        val locationResult: LocationResult = mockk()
  //        val location: android.location.Location = mockk()
  //
  //        every { location.latitude } returns 46.519653
  //        every { location.longitude } returns 6.632273
  //        every { locationResult.lastLocation } returns location
  //
  //        service.onCreate()
  //        locationCallbackSlot.captured.onLocationResult(locationResult)
  //    }
}
