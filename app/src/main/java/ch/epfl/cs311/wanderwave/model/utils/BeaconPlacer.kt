package ch.epfl.cs311.wanderwave.model.utils

import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import com.google.android.gms.maps.model.LatLng
import kotlin.math.*
import kotlin.random.Random

private const val EARTH_RADIUS_M = 6371000.0
private const val BEACON_RADIUS = 1000.0
private const val BEACON_COUNT = 20
private const val NUMBER_ITERATION = 5

/**
 * This function places beacons randomly in the vicinity of the user's location.
 * It first checks if there are enough beacons in the vicinity of the user's location.
 * If not, it generates random beacons and computes the distance between the new beacons and the existing beacons.
 * It then returns the beacons that are to be added.
 *
 * @param beacons the list of existing beacons
 * @param location the user's location
 * @return the beacons that are to be added
 * @author Menzo Bouaissi
 * @since 2.0
 * @last update 2.0
 */
fun placeBeaconsRandomly(beacons: List<Beacon>, location: LatLng): List<Beacon> {
  var finalBeacons = mutableListOf<Beacon>()
  var nearbyBeacons = findNearbyBeacons(location, beacons, BEACON_RADIUS)
  if (nearbyBeacons.size < BEACON_COUNT) {
    var currentMaxDistance = 0.0
    repeat(NUMBER_ITERATION) {
      val newBeacons = mutableListOf<Beacon>()
      repeat(BEACON_COUNT - nearbyBeacons.size) { findRandomBeacon(location, newBeacons, it) }
      var newMaxDistance = computeDistanceBetweenBeacons(newBeacons, beacons)
      if (newMaxDistance > currentMaxDistance || finalBeacons.isEmpty()) {
        currentMaxDistance = newMaxDistance
        finalBeacons = newBeacons
      }
    }
  }
  return finalBeacons
}

/**
 * This function computes the distance between the new beacons and the existing beacons.
 *
 * @param newBeacons the list of new beacons
 * @param beacons the list of existing beacons
 * @return the distance between the new beacons and the existing beacons
 * @author Menzo Bouaissi
 * @since 2.0
 * @last update 2.0
 */
fun computeDistanceBetweenBeacons(newBeacons: MutableList<Beacon>, beacons: List<Beacon>): Double {
  var distance = 0.0
  newBeacons.forEach { beacon ->
    beacons.forEach { existingBeacon ->
      distance += haversine(beacon.location.toLatLng(), existingBeacon.location.toLatLng())
    }
  }
  return distance
}

/**
 * This function generates a random beacon in the vicinity of the user's location.
 *
 * @param location the user's location
 * @param newBeacons the list of new beacons
 * @param it the iteration number
 * @author Menzo Bouaissi
 * @since 2.0
 * @last update 2.0
 */
fun findRandomBeacon(location: LatLng, newBeacons: MutableList<Beacon>, it: Int) {
  val randomLocation = randomLatLongFromPosition(location, BEACON_RADIUS)
  val newBeacon =
      Beacon(
          id = "beacon${newBeacons.size + it}", // TODO: modify the ID!!!!!!!!
          location = Location(randomLocation.latitude, randomLocation.longitude))
  newBeacons.add(newBeacon)
}

/**
 * This function computes the haversine distance between two locations.
 *
 * @param position1 the first location
 * @param position2 the second location
 * @return the haversine distance between the two locations in meters
 * @see <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine formula</a>
 * @author Menzo Bouaissi
 * @since 2.0
 * @last update 2.0
 *
 * */
fun haversine(position1: LatLng, position2: LatLng): Double {
  val latDistance = Math.toRadians(position2.latitude - position1.latitude)
  val lonDistance = Math.toRadians(position2.longitude - position1.longitude)
  val a =
      sin(latDistance / 2).pow(2) +
          (cos(Math.toRadians(position1.latitude)) *
              cos(Math.toRadians(position2.latitude)) *
              sin(lonDistance / 2).pow(2))
  val c = 2 * atan2(sqrt(a), sqrt(1 - a))
  return EARTH_RADIUS_M * c
}

/**
 * This function finds the beacons that are in the vicinity of the user's location.
 * It computes the haversine distance between the user's location and each beacon's location.
 * If the distance is less than the radius, the beacon is added to the list of nearby beacons.
 * The function then returns the list of nearby beacons.
 *
 * @param userPosition the user's location
 * @param beacons the list of existing beacons
 * @param radius the radius in which the beacons are considered to be nearby
 * @return the list of nearby beacons
 * @author Menzo Bouaissi
 * @since 2.0
 * @last update 2.0
 */
fun findNearbyBeacons(userPosition: LatLng, beacons: List<Beacon>, radius: Double): List<Beacon> {
  var nearbyBeacons = mutableListOf<Beacon>()
  beacons.forEach { beacon ->
    if ((haversine(userPosition, beacon.location.toLatLng())) < radius) {
      nearbyBeacons += beacon
    }
  }
  return nearbyBeacons
}

/**
 * This function generates a random latitude and longitude from a given position and distance.
 * It uses the haversine formula to compute the new latitude and longitude.
 *
 * @param userPosition the user's location
 * @param distance the distance from the user's location
 * @return the random latitude and longitude from the user's location and distance
 * @author Menzo Bouaissi
 * @since 2.0
 * @last update 2.0
 */
fun randomLatLongFromPosition(userPosition: LatLng, distance: Double): LatLng {
  val latRad = Math.toRadians(userPosition.latitude)
  val lonRad = Math.toRadians(userPosition.longitude)

  val bearing = Random.nextDouble(0.0, 2 * Math.PI)
  val angularDistance = Random.nextDouble(distance) / EARTH_RADIUS_M

  // New latitude in radians
  val newLat =
      asin(sin(latRad) * cos(angularDistance) + cos(latRad) * sin(angularDistance) * cos(bearing))

  // New longitude in radians
  val newLon =
      lonRad +
          atan2(
              sin(bearing) * sin(angularDistance) * cos(latRad),
              cos(angularDistance) - sin(latRad) * sin(newLat))

  val newLatDeg = Math.toDegrees(newLat)
  val newLonDeg = Math.toDegrees(newLon)

  return LatLng(newLatDeg, newLonDeg)
}
