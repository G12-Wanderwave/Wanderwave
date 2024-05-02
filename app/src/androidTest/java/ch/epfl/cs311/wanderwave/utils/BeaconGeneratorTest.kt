package ch.epfl.cs311.wanderwave.utils

import android.Manifest
import android.content.Context
import android.location.LocationManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import ch.epfl.cs311.wanderwave.BuildConfig
import ch.epfl.cs311.wanderwave.BuildConfig.MAPS_API_KEY
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.utils.createNearbyBeacons
import ch.epfl.cs311.wanderwave.model.utils.getNearbyPOIs
import ch.epfl.cs311.wanderwave.model.utils.types
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.invoke
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.mockkStatic
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BeaconGeneratorTest {
  @get:Rule val mockkRule = MockKRule(this)
  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_COARSE_LOCATION,
          Manifest.permission.ACCESS_FINE_LOCATION,
      )
  private val context = InstrumentationRegistry.getInstrumentation().context

  private val locationManager =
      context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
  val location =
      android.location.Location(LocationManager.GPS_PROVIDER).apply {
        latitude = 46.519962
        longitude = 6.633597
        time = System.currentTimeMillis()
        elapsedRealtimeNanos = System.nanoTime()
      }

  @Before
  fun setup() {
    MockKAnnotations.init(this)
    try {
      locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)
      locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location)
    } catch (e: SecurityException) {
      e.printStackTrace()
    }
  }

  @Test
  fun createNearbyBeacons_addsNewBeaconsWhenNearbyPOIsAreFarFromExistingBeacons() {
    val location = Location(46.519962, 6.633597)

    val nearbyBeacons =
        listOf(
            Beacon(
                id = "testBeacon",
                location = Location(46.519962, 6.633597, "Test Location"),
                tracks = listOf(Track("testTrack", "Test Title", "Test Artist"))))

    val context = ApplicationProvider.getApplicationContext<Context>()

    mockkStatic("ch.epfl.cs311.wanderwave.model.utils.BeaconGeneratorKt")
    every { getNearbyPOIs(any(), any(), any()) } returns
        listOf(
            Location(0.0, 0.0),
        )
    val result = createNearbyBeacons(location, nearbyBeacons, 1000.0, context)

    assertTrue(result.isNotEmpty())
  }

  @Test
  fun createNearbyBeacons_NobeaconsAdded() {
    val location = Location(46.519962, 6.633597)

    val nearbyBeacons =
        listOf(
            Beacon(
                id = "testBeacon",
                location = Location(46.519962, 6.633597, "Test Location"),
                tracks = listOf(Track("testTrack", "Test Title", "Test Artist"))))

    val context = ApplicationProvider.getApplicationContext<Context>()

    mockkStatic("ch.epfl.cs311.wanderwave.model.utils.BeaconGeneratorKt")
    every { getNearbyPOIs(any(), any(), any()) } returns
        listOf(
            Location(46.519962, 6.633591),
        )
    val result = createNearbyBeacons(location, nearbyBeacons, 1000.0, context)

    assertTrue(result.isEmpty())
  }

  @Test
  fun createNearbyBeacons_addsOneBeaconOnly() {
    val location = Location(46.519962, 6.633597)

    val nearbyBeacons =
        listOf(
            Beacon(
                id = "testBeacon",
                location = Location(46.519962, 6.633597, "Test Location"),
                tracks = listOf(Track("testTrack", "Test Title", "Test Artist"))))

    val context = ApplicationProvider.getApplicationContext<Context>()

    mockkStatic("ch.epfl.cs311.wanderwave.model.utils.BeaconGeneratorKt")
    every { getNearbyPOIs(any(), any(), any()) } returns
        listOf(Location(0.0000001, 6.2), Location(0.0, 6.2))
    val result = createNearbyBeacons(location, nearbyBeacons, 1000.0, context)

    assertTrue(result.isNotEmpty())
    assertEquals(1, result.size)
  }

  @Test
  fun createNearbyBeaconsWithNegativeRadius() {
    val location = Location(46.519962, 6.633597)

    val nearbyBeacons =
        listOf(
            Beacon(
                id = "testBeacon",
                location = Location(46.519962, 6.633597, "Test Location"),
                tracks = listOf(Track("testTrack", "Test Title", "Test Artist"))))

    val context = ApplicationProvider.getApplicationContext<Context>()

    mockkStatic("ch.epfl.cs311.wanderwave.model.utils.BeaconGeneratorKt")
    every { getNearbyPOIs(any(), any(), any()) } returns
        listOf(Location(0.0000001, 6.2), Location(0.0, 6.2))

    // Assert that IllegalArgumentException is thrown for negative radius
    val exception =
        assertThrows(IllegalArgumentException::class.java) {
          createNearbyBeacons(location, nearbyBeacons, -1.0, context)
        }

    assertEquals("Radius must be positive", exception.message)
  }

  @Test
  fun testGetNearbyPOIs_PermissionGranted_ReturnsPOIs() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val location = Location(46.519962, 6.633597)
    val radius = 1000.0
    val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
    Places.initialize(context, BuildConfig.MAPS_API_KEY)

    // Mock the response
    val mockResponse = mockk<FindCurrentPlaceResponse>()
    val mockPlaceLikelihood = mockk<PlaceLikelihood>()
    val mockPlace = mockk<Place>()
    every { mockPlace.name } returns "Test Place"
    every { mockPlace.latLng } returns com.google.android.gms.maps.model.LatLng(46.519962, 6.633597)
    every { mockPlaceLikelihood.place } returns mockPlace
    every { mockResponse.placeLikelihoods } returns listOf(mockPlaceLikelihood)
    // Mock the request and the task
    val request = FindCurrentPlaceRequest.newInstance(placeFields)
    val mockTask = mockk<Task<FindCurrentPlaceResponse>>()

    every { mockTask.isSuccessful } returns true
    every { mockTask.result } returns mockResponse
    every {
      mockTask.addOnSuccessListener(any<OnSuccessListener<FindCurrentPlaceResponse>>())
    } answers
        {
          val listener = arg<OnSuccessListener<FindCurrentPlaceResponse>>(0)
          listener.onSuccess(mockResponse)
          mockTask
        }
    every { mockTask.addOnFailureListener(any()) } answers { mockTask }

    val result = getNearbyPOIs(context, location, radius)

    // Assertions
    assertTrue(result.isEmpty())
  }

  @Test
  fun testGetNearbyPOIs_ThrowsRuntimeException() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val location = Location(46.519962, 6.633597)
    val radius = 1000.0
    val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
    Places.initialize(context, BuildConfig.MAPS_API_KEY)

    // Mock the response
    val mockResponse = mockk<FindCurrentPlaceResponse>()
    val mockPlaceLikelihood = mockk<PlaceLikelihood>()
    val mockPlace = mockk<Place>()
    every { mockPlace.name } returns "Test Place"
    every { mockPlace.latLng } returns com.google.android.gms.maps.model.LatLng(46.519962, 6.633597)
    every { mockPlaceLikelihood.place } returns mockPlace
    every { mockResponse.placeLikelihoods } returns listOf(mockPlaceLikelihood)

    // Mock the PlacesClient and its findCurrentPlace method
    val placesClient = mockk<PlacesClient>()
    val task = mockk<Task<FindCurrentPlaceResponse>>()
    every { task.isSuccessful } returns false
    every { task.exception } returns NoSuchFieldException()
    every { placesClient.findCurrentPlace(any()) } returns task

    // Mock the request and the task
    val request = FindCurrentPlaceRequest.newInstance(placeFields)
    val mockTask = mockk<Task<FindCurrentPlaceResponse>>()

    every {
      mockTask.addOnSuccessListener(any<OnSuccessListener<FindCurrentPlaceResponse>>())
    } answers
        {
          val listener = arg<OnSuccessListener<FindCurrentPlaceResponse>>(0)
          listener.onSuccess(mockResponse)
          mockTask
        }
    every { mockTask.addOnFailureListener(any()) } answers { mockTask }

    val result = getNearbyPOIs(context, location, radius)

    // Assertions
    assertTrue(result.isEmpty())
  }

  @Test
  fun typesList_containsExpectedElements() {
    val expectedTypes =
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
            "subway_station",
            "synagogue",
            "tourist_attraction",
            "train_station",
            "transit_station",
            "university",
            "zoo")

    assertEquals(expectedTypes, types)
  }
}
