package ch.epfl.cs311.wanderwave.ui.components.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import ch.epfl.cs311.wanderwave.BuildConfig.MAPS_API_KEY
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest

// TODO: What would be a good value for these ?
private const val MIN_REVIEWS = 100
private const val MIN_BEACON_DISTANCE = 100.0

// TODO: Store as a resource
// TODO: Which types do we want ?
val types: List<String> =
    listOf(
        "airport",
        "amusement_park",
        "aquarium",
        "art_gallery",
        "bowling_alley",
        "campground",
        "casino",
        "church",
        "city_hall",
        "courthouse",
        "embassy",
        "fire_station",
        "hospital",
        "library",
        "light_rail_station",
        "local_government_office",
        "movie_theater",
        "museum",
        "park",
        "stadium",
        "subway_station",
        "synagogue",
        "tourist_attraction",
        "train_station",
        "transit_station",
        "university",
        "zoo")

fun createNearbyBeacons(
    location: Location,
    nearbyBeacons: List<Beacon>,
    radius: Double,
    context: Context
): List<Beacon> {
  // Initialize the list of new Beacons
  val newBeacons = mutableListOf<Beacon>()

  // Get the nearby points of interest
  val nearbyPOIs = getNearbyPOIs(context, location, radius)

  // Place new beacons at the nearby points of interest
  for (poi in nearbyPOIs) {
    // Check if POI is far enough from existing beacons
    if (nearbyBeacons.any() { beacon ->
      beacon.location.distanceBetween(poi) > MIN_BEACON_DISTANCE
    }) {
      newBeacons.add(Beacon("", poi))
    }
  }

  return newBeacons
}

/**
 * Get the nearby points of interest (POIs) using the Places API.
 *
 * @param context The context of the application.
 * @param location The current location of the user.
 * @param radius The radius in which to search for POIs.
 * @return A list of nearby POIs.
 */
fun getNearbyPOIs(context: Context, location: Location, radius: Double): List<Location> {
  // Initialize the list of nearby POIs
  val nearbyPOIs = mutableListOf<Location>()

  // Initialize the Places API
  Places.initialize(context, MAPS_API_KEY)

  // Create a new Places client instance
  val placesClient = Places.createClient(context)

  // Define the fields to request
  val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

  // Create a new FindCurrentPlaceRequest
  val request = FindCurrentPlaceRequest.newInstance(placeFields)

  // Check if the app has the required permissions
  if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
      PackageManager.PERMISSION_GRANTED) {
    // Use the Places API to find nearby places
    placesClient
        .findCurrentPlace(request)
        .addOnSuccessListener { response ->
          for (placeLikelihood in response.placeLikelihoods) {
            val place = placeLikelihood.place
            // Conversion to make computing distances easier
            val placeLoc = Location(place.latLng.latitude, place.latLng.longitude, place.name)

            if (location.distanceBetween(placeLoc) <=
                radius && // Check if the place is within the radius
                place.reviews.size >= MIN_REVIEWS && // Check if the place has enough reviews
                place.placeTypes.any() {
                  types.contains(it)
                } // Check if the place is of a certain type
            ) {
              nearbyPOIs.add(placeLoc)
              Log.d("PlacesApi", "Place found: ${place.name} at ${place.latLng}")
            }
          }
        }
        .addOnFailureListener { exception ->
          if (exception is ApiException) {
            Log.d("PlacesApi", "Place not found: ${exception.statusCode}")
          }
        }
  }

  return nearbyPOIs
}
