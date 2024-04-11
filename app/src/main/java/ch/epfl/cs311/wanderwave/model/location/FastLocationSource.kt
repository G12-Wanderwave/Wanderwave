package ch.epfl.cs311.wanderwave.model.location

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.LocationSource

class FastLocationSource(private val context: Context) : LocationSource, LocationListener {
  private var listener: LocationSource.OnLocationChangedListener? = null
  private var locationClient: FusedLocationProviderClient? = null

  @RequiresApi(Build.VERSION_CODES.S)
  @RequiresPermission(
      allOf =
          [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
  override fun activate(onLocationChangedListener: LocationSource.OnLocationChangedListener) {
    listener = onLocationChangedListener
    locationClient = LocationServices.getFusedLocationProviderClient(context)
    val locationRequest = LocationRequest.Builder(5)
      .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
      .build()
    locationClient?.requestLocationUpdates(locationRequest, this, null)
  }

  override fun deactivate() {
    locationClient?.removeLocationUpdates(this)
    locationClient = null
    listener = null
  }

  override fun onLocationChanged(location: Location) {
    listener?.onLocationChanged(location)
  }
}
