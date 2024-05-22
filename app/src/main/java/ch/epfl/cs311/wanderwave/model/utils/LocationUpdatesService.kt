package ch.epfl.cs311.wanderwave.model.utils

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ch.epfl.cs311.wanderwave.MainActivity
import com.google.android.gms.location.*

class LocationUpdatesService(private val testContext: Context? = null) : Service() {

  companion object {
    const val ACTION_LOCATION_BROADCAST = "ch.epfl.cs311.wanderwave.LOCATION_BROADCAST"
    const val EXTRA_LATITUDE = "ch.epfl.cs311.wanderwave.LATITUDE"
    const val EXTRA_LONGITUDE = "ch.epfl.cs311.wanderwave.LONGITUDE"
  }

  private lateinit var fusedLocationClient: FusedLocationProviderClient
  private lateinit var locationCallback: LocationCallback

    val context = testContext ?: this
  override fun onCreate() {
    super.onCreate()
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    locationCallback =
        object : LocationCallback() {
          override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            if (location != null) {
              sendLocationBroadcast(location.latitude, location.longitude)
            }
          }
        }

    val locationRequest =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 600000)
            .setMinUpdateIntervalMillis(30000)
            .build()

    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
      fusedLocationClient.requestLocationUpdates(
          locationRequest, locationCallback, Looper.getMainLooper())
    }

    startForegroundService()
  }

  fun sendLocationBroadcast(latitude: Double, longitude: Double) {
    val intent =
        Intent(ACTION_LOCATION_BROADCAST).apply {
          putExtra(EXTRA_LATITUDE, latitude)
          putExtra(EXTRA_LONGITUDE, longitude)
        }
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
  }

  fun startForegroundService() {
    if (context==testContext) {
      return
    }
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

        val channel =
          NotificationChannel(
              "location_updates_channel",
              "Location Updates",
              NotificationManager.IMPORTANCE_DEFAULT)
      notificationManager.createNotificationChannel(channel)
    }

    val notificationIntent = Intent(this, MainActivity::class.java)
    val pendingIntent =
        PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    val notification: Notification =
        NotificationCompat.Builder(this, "location_updates_channel")
            .setContentIntent(pendingIntent)
            .build()

    startForeground(1, notification)
  }

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  override fun onDestroy() {
    super.onDestroy()
    fusedLocationClient.removeLocationUpdates(locationCallback)
  }
}
