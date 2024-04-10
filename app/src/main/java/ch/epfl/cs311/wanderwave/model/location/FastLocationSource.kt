package ch.epfl.cs311.wanderwave.model.location

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.google.android.gms.maps.LocationSource

class FastLocationSource(private val context: Context) : LocationSource, LocationListener {
  private var listener: LocationSource.OnLocationChangedListener? = null
  private var locationManager: LocationManager? = null

  @RequiresApi(Build.VERSION_CODES.S)
  @RequiresPermission(
      allOf =
          [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
  override fun activate(onLocationChangedListener: LocationSource.OnLocationChangedListener) {
    listener = onLocationChangedListener
    locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    locationManager?.requestLocationUpdates(LocationManager.FUSED_PROVIDER, 0, 0f, this)
  }

  override fun deactivate() {
    locationManager?.removeUpdates(this)
    locationManager = null
    listener = null
  }

  override fun onLocationChanged(location: Location) {
    listener?.onLocationChanged(location)
  }
}
