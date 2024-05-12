package ch.epfl.cs311.wanderwave.model.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import ch.epfl.cs311.wanderwave.BuildConfig
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import kotlin.math.*
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

private const val EARTH_RADIUS = 6371.0
private const val BEACON_RADIUS = 2.0
private const val BEACON_COUNT = 20
private const val MIN_BEACON_DISTANCE = 0.1

val types: List<String> =
    listOf(
        "airport",
        "amusement_park",
        "aquarium",
        "art_gallery",
        "spa",
        "bowling_alley",
        "cafe",
        "campground",
        "casino",
        "church",
        "city_hall",
        "courthouse",
        "drugstore",
        "embassy",
        "establishment",
        "fire_station",
        "hindu_temple",
        "hospital",
        "library",
        "light_rail_station",
        "local_government_office",
        "movie_theater",
        "museum",
        "park",
        "primary_school",
        "spa",
        "stadium",
        "store",
        "subway_station",
        "synagogue",
        "tourist_attraction",
        "train_station",
        "transit_station",
        "university",
        "zoo")

/**
 * This function places beacons randomly in the vicinity of the user's location. It first checks if
 * there are enough beacons in the vicinity of the user's location. If not, it generates random
 * beacons and computes the distance between the new beacons and the existing beacons. It then
 * returns the beacons that are to be added.
 *
 * @param beacons the list of existing beacons
 * @param location the user's location
 * @return the beacons that are to be added
 * @author Menzo Bouaissi
 * @since 2.0
 * @last update 2.0
 */
fun placeBeaconsRandomly(
    beacons: List<Beacon>,
    location: Location,
    beaconRepository: BeaconRepository
): List<Beacon> {
  val newBeacons = mutableListOf<Beacon>()
  var nearbyBeacons = findNearbyBeacons(location, beacons)
  if (nearbyBeacons.size < BEACON_COUNT) {
    repeat(BEACON_COUNT - nearbyBeacons.size) {
      findRandomBeacon(location, newBeacons, it, beaconRepository)
    }
  }
  return newBeacons
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
      distance += beacon.location.distanceBetween(existingBeacon.location)
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
fun findRandomBeacon(
    location: Location,
    newBeacons: MutableList<Beacon>,
    it: Int,
    beaconRepository: BeaconRepository
) {
  val randomLocation = randomLatLongFromPosition(location, BEACON_RADIUS)
  val newBeacon =
      Beacon(
          id = "", // TODO a modifier
          location = Location(randomLocation.latitude, randomLocation.longitude))

  newBeacons.add(newBeacon)
}

/**
 * This function finds the beacons that are in the vicinity of the user's location. It computes the
 * haversine distance between the user's location and each beacon's location. If the distance is
 * less than the radius, the beacon is added to the list of nearby beacons. The function then
 * returns the list of nearby beacons.
 *
 * @param userPosition the user's location
 * @param beacons the list of existing beacons
 * @param radius the radius in which the beacons are considered to be nearby
 * @return the list of nearby beacons
 * @author Menzo Bouaissi
 * @since 2.0
 * @last update 2.0
 */
fun findNearbyBeacons(userPosition: Location, beacons: List<Beacon>): List<Beacon> {
  var nearbyBeacons = mutableListOf<Beacon>()
  beacons.forEach { beacon ->
    if ((userPosition.distanceBetween(beacon.location)) < BEACON_RADIUS) {
      nearbyBeacons += beacon
    }
  }
  return nearbyBeacons
}

/**
 * @param userPosition the user's location
 * @param beacons the list of existing beacons
 * @param radius the radius in which the beacons are considered to be nearby
 * @return the list of nearby beacons
 * @author Menzo Bouaissi
 * @since 2.0
 * @last update 2.0
 */
fun hasEnoughBeacons(userPosition: Location, beacons: List<Beacon>): Boolean {
  var nearbyBeacons = mutableListOf<Beacon>()
  beacons.forEach {
    if ((userPosition.distanceBetween(it.location)) < BEACON_RADIUS) {
      nearbyBeacons += it
    }
  }
  return nearbyBeacons.size == BEACON_COUNT
}

/**
 * This function generates a random latitude and longitude from a given position and distance. It
 * uses the haversine formula to compute the new latitude and longitude.
 *
 * @param userPosition the user's location
 * @param distance the distance from the user's location
 * @return the random latitude and longitude from the user's location and distance
 * @author Menzo Bouaissi
 * @since 2.0
 * @last update 2.0
 */
fun randomLatLongFromPosition(userPosition: Location, distance: Double): Location {
  val latRad = Math.toRadians(userPosition.latitude)
  val lonRad = Math.toRadians(userPosition.longitude)

  val bearing = Random.nextDouble(0.0, 2 * Math.PI)
  val angularDistance = Random.nextDouble(distance) / EARTH_RADIUS

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

  return Location(newLatDeg, newLonDeg)
}

/**
 * Create new beacons at nearby points of interest.
 *
 * @param location The current location of the user.
 * @param nearbyBeacons The list of existing beacons.
 * @param radius The radius in which to search for points of interest.
 * @param context The context of the application.
 * @return A list of new beacons.
 */
fun createNearbyBeacons(
    location: Location,
    nearbyBeacons: MutableStateFlow<List<Beacon>>,
    radius: Double,
    context: Context,
    beaconRepository: BeaconRepository,
    scope: CoroutineScope,
    onComplete: () -> Unit,
) {
  scope.launch {
    if (radius <= 0.0) {
      throw IllegalArgumentException("Radius must be positive")
    }

    val newBeacons = mutableListOf<Beacon>()
    val nearbyPOIs = MutableStateFlow<List<Location>>(emptyList())

    // Pass the `onComplete` callback to `getNearbyPOIs`
    getNearbyPOIs(
        context,
        location,
        radius,
        nearbyPOIs,
        nearbyBeacons,
        newBeacons,
        beaconRepository,
        scope,
        onComplete)
  }
}

/**
 * Get the nearby points of interest (POIs) using the Places API.
 *
 * @param context The context of the application.
 * @param location The current location of the user.
 * @param radius The radius in which to search for POIs.
 * @return A list of nearby POIs.
 */
suspend fun getNearbyPOIs(
    context: Context,
    location: Location,
    radius: Double,
    nearbyPOIs: MutableStateFlow<List<Location>>,
    nearbyBeacons: MutableStateFlow<List<Beacon>>,
    newBeacons: MutableList<Beacon>,
    beaconRepository: BeaconRepository,
    scope: CoroutineScope,
    onComplete: () -> Unit
) {
  Places.initialize(context, BuildConfig.MAPS_API_KEY)
  val placesClient = Places.createClient(context)

  val placeFieldID = Place.Field.ID
  val placeFieldName = Place.Field.NAME
  val placeFieldLatLng = Place.Field.LAT_LNG
  val placeFieldTypes = Place.Field.TYPES
  val placeFieldRating = Place.Field.RATING

  val placeFields =
      listOf(placeFieldID, placeFieldName, placeFieldLatLng, placeFieldTypes, placeFieldRating)

  val request = FindCurrentPlaceRequest.newInstance(placeFields)

  if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
      PackageManager.PERMISSION_GRANTED) {
    placesClient
        .findCurrentPlace(request)
        .addOnSuccessListener { response ->
          for (placeLikelihood in response.placeLikelihoods) {
            val place = placeLikelihood.place
            val placeLoc = Location(place.latLng.latitude, place.latLng.longitude, place.name)
            if ((location.distanceBetween(placeLoc) <= radius) &&
                place.placeTypes.any { types.contains(it) })
                nearbyPOIs.value += placeLoc
          }
          nearbyPOIs.value.forEach { poi ->
            if (nearbyBeacons.value.all {
              it.location.distanceBetween(poi) > MIN_BEACON_DISTANCE
            } && newBeacons.all { it.location.distanceBetween(poi) > MIN_BEACON_DISTANCE }) {
              val beacon = Beacon(poi.name, poi)
              newBeacons.add(beacon)
              scope.launch {
                val id = beaconRepository.addItemAndGetId(beacon)
                if (id != null) {
                  beacon.copy(id = id, location = poi, profileAndTrack = listOf())
                }
                // beaconRepository.updateItem(beacon)
              }
            }
          }
          nearbyBeacons.value += newBeacons

          // Invoke `onComplete` after processing API results
          onComplete()
        }
        .addOnFailureListener { exception ->
          if (exception is ApiException) {
            Log.e("PlacesApi", "Place not found: ${exception.statusCode}")
            Log.e("PlacesApi", "Place not found: ${exception.message}")
          } else {
            Log.e("Error", "An error occurred: ${exception.message}")
          }

          // Call `onComplete` to ensure failure also triggers it
          onComplete()
        }
  }
}
