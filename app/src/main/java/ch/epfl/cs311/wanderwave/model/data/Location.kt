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

  fun toLocation(): Location {
    return Location(latitude, longitude, name)
  }

  fun toMap(): Map<String, Any> {
    return hashMapOf("latitude" to latitude, "longitude" to longitude, "name" to name)
  }

  /**
   * Calculate the distance between two locations using the Haversine formula.
   *
   * @param location The location to calculate the distance to.
   * @return The distance between the two locations in kilometers.
   */
  fun distanceBetween(location: Location): Double {
    val earthRadius = 6371.0
    val dLat = Math.toRadians(location.latitude - latitude)
    val dLng = Math.toRadians(location.longitude - longitude)
    val a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(latitude)) *
                Math.cos(Math.toRadians(location.latitude)) *
                Math.sin(dLng / 2) *
                Math.sin(dLng / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return earthRadius * c
  }
}
