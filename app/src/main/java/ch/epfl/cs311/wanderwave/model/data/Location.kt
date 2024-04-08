package ch.epfl.cs311.wanderwave.model.data

import com.google.android.gms.maps.model.LatLng

data class Location(
    /** Latitude of the location */
    val latitude: Double,

    /** Longitude of the location */
    val longitude: Double,

    /** Name of the location. Useful if the location corresponds to some point of interest. */
    val name: String = "",
) {
  fun toLatLng(): LatLng {
    return LatLng(latitude, longitude)
  }
}
