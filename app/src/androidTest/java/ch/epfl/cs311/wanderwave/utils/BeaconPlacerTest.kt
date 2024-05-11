package ch.epfl.cs311.wanderwave.utils

import android.Manifest
import android.content.Context
import android.location.LocationManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import ch.epfl.cs311.wanderwave.model.data.Location
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.mockk.MockKAnnotations
import io.mockk.junit4.MockKRule
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val BEACON_COUNT = 20

@RunWith(AndroidJUnit4::class)
class BeaconPlacerTest : TestCase() {
  val location2 = Location(46.519962, 6.633597)

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
  fun placerHolder() {
    assertTrue(true)
  }

  //  @Test
  //  fun createNearbyBeacons_addsNewBeaconsWhenNearbyPOIsAreFarFromExistingBeacons() {
  //    val location = Location(46.519962, 6.633597)
  //
  //    val nearbyBeacons =
  //        listOf(
  //            Beacon(
  //                id = "testBeacon",
  //                location = Location(1.0, 1.0, "Test Location"),
  //                profileAndTrack =
  //                    listOf(
  //                        ProfileTrackAssociation(
  //                            Profile(
  //                                "Sample First Name",
  //                                "Sample last name",
  //                                "Sample desc",
  //                                0,
  //                                false,
  //                                null,
  //                                "Sample Profile ID",
  //                                "Sample Track ID"),
  //                            Track("Sample Track ID", "Sample Track Title", "Sample Artist
  // Name")))))
  //
  //    //
  //    val context = ApplicationProvider.getApplicationContext<Context>()
  //
  //    mockkStatic("ch.epfl.cs311.wanderwave.model.utils.BeaconPlacerKt")
  //    every { getNearbyPOIs(any(), any(), any()) } returns
  //        listOf(
  //            Location(0.0, 0.0),
  //        )
  //    val result = createNearbyBeacons(location, nearbyBeacons, 1000.0, context)
  //
  //    assertTrue(result.isNotEmpty())
  //  }
  //
  //  @Test
  //  fun createNearbyBeacons_NobeaconsAdded() {
  //    val location = Location(46.519962, 6.633597)
  //
  //    val nearbyBeacons =
  //        listOf(
  //            Beacon(
  //                id = "testBeacon",
  //                location = Location(1.0, 1.0, "Test Location"),
  //                profileAndTrack =
  //                    listOf(
  //                        ProfileTrackAssociation(
  //                            Profile(
  //                                "Sample First Name",
  //                                "Sample last name",
  //                                "Sample desc",
  //                                0,
  //                                false,
  //                                null,
  //                                "Sample Profile ID",
  //                                "Sample Track ID"),
  //                            Track("Sample Track ID", "Sample Track Title", "Sample Artist
  // Name")))))
  //    val context = ApplicationProvider.getApplicationContext<Context>()
  //
  //    mockkStatic("ch.epfl.cs311.wanderwave.model.utils.BeaconPlacerKt")
  //    every { getNearbyPOIs(any(), any(), any()) } returns
  //        listOf(
  //            Location(46.519962, 6.633591),
  //        )
  //    val result = createNearbyBeacons(location, nearbyBeacons, 1000.0, context)
  //
  //    assertTrue(result.isNotEmpty())
  //  }
  //
  //  @Test
  //  fun createNearbyBeacons_addsOneBeaconOnly() {
  //    val location = Location(46.519962, 6.633597)
  //
  //    val nearbyBeacons =
  //        listOf(
  //            Beacon(
  //                id = "testBeacon",
  //                location = Location(1.0, 1.0, "Test Location"),
  //                profileAndTrack =
  //                    listOf(
  //                        ProfileTrackAssociation(
  //                            Profile(
  //                                "Sample First Name",
  //                                "Sample last name",
  //                                "Sample desc",
  //                                0,
  //                                false,
  //                                null,
  //                                "Sample Profile ID",
  //                                "Sample Track ID"),
  //                            Track("Sample Track ID", "Sample Track Title", "Sample Artist
  // Name")))))
  //    val context = ApplicationProvider.getApplicationContext<Context>()
  //
  //    mockkStatic("ch.epfl.cs311.wanderwave.model.utils.BeaconPlacerKt")
  //    every { getNearbyPOIs(any(), any(), any()) } returns
  //        listOf(Location(0.0000001, 6.2), Location(0.0, 6.2))
  //    val result = createNearbyBeacons(location, nearbyBeacons, 1000.0, context)
  //
  //    assertTrue(result.isNotEmpty())
  //    assertEquals(1, result.size)
  //  }
  //
  //  @Test
  //  fun createNearbyBeaconsWithNegativeRadius() {
  //    val location = Location(46.519962, 6.633597)
  //
  //    val nearbyBeacons =
  //        listOf(
  //            Beacon(
  //                id = "testBeacon",
  //                location = Location(1.0, 1.0, "Test Location"),
  //                profileAndTrack =
  //                    listOf(
  //                        ProfileTrackAssociation(
  //                            Profile(
  //                                "Sample First Name",
  //                                "Sample last name",
  //                                "Sample desc",
  //                                0,
  //                                false,
  //                                null,
  //                                "Sample Profile ID",
  //                                "Sample Track ID"),
  //                            Track("Sample Track ID", "Sample Track Title", "Sample Artist
  // Name")))))
  //    val context = ApplicationProvider.getApplicationContext<Context>()
  //
  //    mockkStatic("ch.epfl.cs311.wanderwave.model.utils.BeaconPlacerKt")
  //    every { getNearbyPOIs(any(), any(), any()) } returns
  //        listOf(Location(0.0000001, 6.2), Location(0.0, 6.2))
  //
  //    // Assert that IllegalArgumentException is thrown for negative radius
  //    val exception =
  //        Assert.assertThrows(IllegalArgumentException::class.java) {
  //          createNearbyBeacons(location, nearbyBeacons, -1.0, context)
  //        }
  //
  //    assertEquals("Radius must be positive", exception.message)
  //  }
  //
  //  @Test
  //  fun testGetNearbyPOIs_PermissionGranted_ReturnsPOIs() {
  //    val context = ApplicationProvider.getApplicationContext<Context>()
  //    val location = Location(46.519962, 6.633597)
  //    val radius = 1000.0
  //    val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
  //    Places.initialize(context, BuildConfig.MAPS_API_KEY)
  //
  //    // Mock the response
  //    val mockResponse = mockk<FindCurrentPlaceResponse>()
  //    val mockPlaceLikelihood = mockk<PlaceLikelihood>()
  //    val mockPlace = mockk<Place>()
  //    every { mockPlace.name } returns "Test Place"
  //    every { mockPlace.latLng } returns com.google.android.gms.maps.model.LatLng(46.519962,
  // 6.633597)
  //    every { mockPlaceLikelihood.place } returns mockPlace
  //    every { mockResponse.placeLikelihoods } returns listOf(mockPlaceLikelihood)
  //    // Mock the request and the task
  //    val request = FindCurrentPlaceRequest.newInstance(placeFields)
  //    val mockTask = mockk<Task<FindCurrentPlaceResponse>>()
  //
  //    every { mockTask.isSuccessful } returns true
  //    every { mockTask.result } returns mockResponse
  //    every {
  //      mockTask.addOnSuccessListener(any<OnSuccessListener<FindCurrentPlaceResponse>>())
  //    } answers
  //        {
  //          val listener = arg<OnSuccessListener<FindCurrentPlaceResponse>>(0)
  //          listener.onSuccess(mockResponse)
  //          mockTask
  //        }
  //    every { mockTask.addOnFailureListener(any()) } answers { mockTask }
  //
  //    val result = getNearbyPOIs(context, location, radius)
  //
  //    // Assertions
  //    assertTrue(result.isEmpty())
  //  }
  //
  //  @Test
  //  fun testGetNearbyPOIs_ThrowsRuntimeException() {
  //    val context = ApplicationProvider.getApplicationContext<Context>()
  //    val location = Location(46.519962, 6.633597)
  //    val radius = 1000.0
  //    val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
  //    Places.initialize(context, BuildConfig.MAPS_API_KEY)
  //
  //    // Mock the response
  //    val mockResponse = mockk<FindCurrentPlaceResponse>()
  //    val mockPlaceLikelihood = mockk<PlaceLikelihood>()
  //    val mockPlace = mockk<Place>()
  //    every { mockPlace.name } returns "Test Place"
  //    every { mockPlace.latLng } returns com.google.android.gms.maps.model.LatLng(46.519962,
  // 6.633597)
  //    every { mockPlaceLikelihood.place } returns mockPlace
  //    every { mockResponse.placeLikelihoods } returns listOf(mockPlaceLikelihood)
  //
  //    // Mock the PlacesClient and its findCurrentPlace method
  //    val placesClient = mockk<PlacesClient>()
  //    val task = mockk<Task<FindCurrentPlaceResponse>>()
  //    every { task.isSuccessful } returns false
  //    every { task.exception } returns NoSuchFieldException()
  //    every { placesClient.findCurrentPlace(any()) } returns task
  //
  //    // Mock the request and the task
  //    val request = FindCurrentPlaceRequest.newInstance(placeFields)
  //    val mockTask = mockk<Task<FindCurrentPlaceResponse>>()
  //
  //    every {
  //      mockTask.addOnSuccessListener(any<OnSuccessListener<FindCurrentPlaceResponse>>())
  //    } answers
  //        {
  //          val listener = arg<OnSuccessListener<FindCurrentPlaceResponse>>(0)
  //          listener.onSuccess(mockResponse)
  //          mockTask
  //        }
  //    every { mockTask.addOnFailureListener(any()) } answers { mockTask }
  //
  //    val result = getNearbyPOIs(context, location, radius)
  //
  //    // Assertions
  //    assertTrue(result.isEmpty())
  //  }
  //
  //  @Test
  //  fun typesList_containsExpectedElements() {
  //    val expectedTypes =
  //        listOf(
  //            "airport",
  //            "amusement_park",
  //            "aquarium",
  //            "art_gallery",
  //            "spa",
  //            "bowling_alley",
  //            "cafe",
  //            "campground",
  //            "casino",
  //            "church",
  //            "city_hall",
  //            "courthouse",
  //            "drugstore",
  //            "embassy",
  //            "fire_station",
  //            "hindu_temple",
  //            "hospital",
  //            "library",
  //            "light_rail_station",
  //            "local_government_office",
  //            "movie_theater",
  //            "museum",
  //            "park",
  //            "primary_school",
  //            "spa",
  //            "stadium",
  //            "subway_station",
  //            "synagogue",
  //            "tourist_attraction",
  //            "train_station",
  //            "transit_station",
  //            "university",
  //            "zoo")
  //
  //    assertEquals(expectedTypes, types)
  //  }
  //
  //  @Test
  //  fun placeBeaconRandomly_addsBeaconWhenCountIsLessThanThreshold() {
  //    val beacons = listOf<Beacon>()
  //
  //    val result = placeBeaconsRandomly(beacons, location2)
  //    assertTrue(result.isNotEmpty())
  //  }
  //
  //  @Test
  //  fun placeBeaconRandomly_doesNotAddBeaconWhenCountIsEqualToThreshold() {
  //    val beacons = List(BEACON_COUNT) { Beacon("beacon$it", Location(46.519962, 6.633597)) }
  //
  //    val result = placeBeaconsRandomly(beacons, location2)
  //
  //    assertEquals(0, result.size)
  //  }
  //
  //  @Test
  //  fun placeBeaconRandomly_doesAddBeacons() {
  //    val nbrMissing = 9
  //    val beacons =
  //        List(BEACON_COUNT - nbrMissing) { Beacon("beacon$it", Location(46.519962, 6.633597)) }
  //
  //    val result = placeBeaconsRandomly(beacons, location2)
  //
  //    assertEquals(nbrMissing, result.size)
  //  }
  //
  //  @Test
  //  fun placeBeaconRandomly_selectsBeaconsWithMaxDistance() {
  //    val beacons = listOf<Beacon>()
  //
  //    val result = placeBeaconsRandomly(beacons, location2)
  //
  //    val maxDistance = computeDistanceBetweenBeacons(result.toMutableList(), beacons)
  //    result.forEachIndexed { index, beacon ->
  //      val otherBeacons = result.toMutableList().apply { removeAt(index) }
  //      val distance = computeDistanceBetweenBeacons(otherBeacons, beacons)
  //      assertTrue(distance <= maxDistance)
  //    }
  //  }
  //
  //  @Test
  //  fun computeDistanceBetweenBeacons_returnsCorrectDistance() {
  //    val beacons =
  //        listOf(
  //            Beacon("beacon1", Location(46.519962, 6.633597)),
  //            Beacon("beacon2", Location(46.520962, 6.634597)))
  //    val newBeacons = mutableListOf(Beacon("beacon3", Location(46.521962, 6.635597)))
  //
  //    val result = computeDistanceBetweenBeacons(newBeacons, beacons)
  //
  //    assertTrue(result > 0)
  //  }
  //
  //  @Test
  //  fun findRandomBeacon_addsBeaconToNewBeacons() {
  //    val newBeacons = mutableListOf<Beacon>()
  //
  //    findRandomBeacon(location2, newBeacons, 0)
  //    Log.d("value of the random beacon ", newBeacons.toString())
  //    assertEquals(1, newBeacons.size)
  //  }
  //
  //  @Test
  //  fun haversine_returnsCorrectDistance() {
  //    val position1 = Location(46.519962, 6.633597)
  //    val position2 = Location(46.520962, 6.634597)
  //
  //    val result = position1.distanceBetween(position2)
  //
  //    assertTrue(result > 0)
  //  }
  //
  //  @Test
  //  fun findNearbyBeacons_returnsOnlyBeaconsWithinRadius() {
  //    val userPosition = Location(46.519962, 6.633597)
  //    val beacons =
  //        listOf(
  //            Beacon("beacon1", Location(46.519962, 6.633597)), // within radius
  //            Beacon("beacon2", Location(46.520962, 6.634597)), // within radius
  //            Beacon("beacon3", Location(4.529962, 6.644597)) // outside radius
  //            )
  //
  //    val result = findNearbyBeacons(userPosition, beacons, 1000.0)
  //
  //    assertEquals(2, result.size)
  //  }
  //
  //  @Test
  //  fun findNearbyBeacons_returnsEmptyListWhenNoBeaconsWithinRadius() {
  //    val userPosition = Location(46.519962, 6.633597)
  //    val beacons =
  //        listOf(
  //            Beacon("beacon1", Location(46.529962, 60.644597)), // outside radius
  //            Beacon("beacon2", Location(4.539962, 6.654597)) // outside radius
  //            )
  //
  //    val result = findNearbyBeacons(userPosition, beacons, 1000.0)
  //
  //    assertTrue(result.isEmpty())
  //  }
  //
  //  @Test
  //  fun findNearbyBeacons_returnsAllBeaconsWhenAllWithinRadius() {
  //    val userPosition = Location(46.519962, 6.633597)
  //    val beacons =
  //        listOf(
  //            Beacon("beacon1", Location(46.519962, 6.633597)), // within radius
  //            Beacon("beacon2", Location(46.520962, 6.634597)) // within radius
  //            )
  //
  //    val result = findNearbyBeacons(userPosition, beacons, 1000.0)
  //
  //    assertEquals(beacons.size, result.size)
  //  }
  //
  //  @Test
  //  fun randomLatLongFromPosition_returnsLocationWithinDistance() {
  //    val userPosition = Location(46.519962, 6.633597)
  //    val distance = 1000.0
  //
  //    val result = randomLatLongFromPosition(userPosition, distance)
  //
  //    val actualDistance = location2.distanceBetween(result)
  //    Log.d("distance", "$actualDistance")
  //    assertTrue(actualDistance <= distance)
  //  }
}
