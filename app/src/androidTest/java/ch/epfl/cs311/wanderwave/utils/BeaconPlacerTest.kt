package ch.epfl.cs311.wanderwave.utils

import android.Manifest
import android.content.Context
import android.location.LocationManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.repository.BeaconRepository
import ch.epfl.cs311.wanderwave.model.utils.computeDistanceBetweenBeacons
import ch.epfl.cs311.wanderwave.model.utils.findClosestBeacon
import ch.epfl.cs311.wanderwave.model.utils.findNearbyBeacons
import ch.epfl.cs311.wanderwave.model.utils.findRandomBeacon
import ch.epfl.cs311.wanderwave.model.utils.hasEnoughBeacons
import ch.epfl.cs311.wanderwave.model.utils.placeBeaconsRandomly
import ch.epfl.cs311.wanderwave.model.utils.randomLatLongFromPosition
import ch.epfl.cs311.wanderwave.model.utils.types
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runBlockingTest
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

  @RelaxedMockK private lateinit var mockBeaconRepository: BeaconRepository

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
  fun typesList_containsExpectedElements() {
    val expectedTypes =
        listOf(
            "accounting",
            "airport",
            "amusement_park",
            "aquarium",
            "art_gallery",
            "atm",
            "bakery",
            "bank",
            "bar",
            "beauty_salon",
            "bicycle_store",
            "book_store",
            "bowling_alley",
            "bus_station",
            "cafe",
            "campground",
            "car_dealer",
            "car_rental",
            "car_repair",
            "car_wash",
            "casino",
            "cemetery",
            "church",
            "city_hall",
            "clothing_store",
            "convenience_store",
            "courthouse",
            "dentist",
            "department_store",
            "doctor",
            "drugstore",
            "electrician",
            "electronics_store",
            "embassy",
            "fire_station",
            "florist",
            "funeral_home",
            "furniture_store",
            "gas_station",
            "gym",
            "hair_care",
            "hardware_store",
            "hindu_temple",
            "home_goods_store",
            "hospital",
            "insurance_agency",
            "jewelry_store",
            "laundry",
            "lawyer",
            "library",
            "light_rail_station",
            "liquor_store",
            "local_government_office",
            "locksmith",
            "lodging",
            "meal_delivery",
            "meal_takeaway",
            "mosque",
            "movie_rental",
            "movie_theater",
            "moving_company",
            "museum",
            "night_club",
            "painter",
            "park",
            "parking",
            "pet_store",
            "pharmacy",
            "physiotherapist",
            "plumber",
            "police",
            "post_office",
            "primary_school",
            "real_estate_agency",
            "restaurant",
            "roofing_contractor",
            "rv_park",
            "school",
            "secondary_school",
            "shoe_store",
            "shopping_mall",
            "spa",
            "stadium",
            "storage",
            "store",
            "subway_station",
            "supermarket",
            "synagogue",
            "taxi_stand",
            "tourist_attraction",
            "train_station",
            "transit_station",
            "travel_agency",
            "university",
            "veterinary_care",
            "zoo")

    assertEquals(expectedTypes, types)
  }

  @Test
  fun placeBeaconRandomly_addsBeaconWhenCountIsLessThanThreshold() {
    val beacons = listOf<Beacon>()

    val result = placeBeaconsRandomly(beacons, location2, mockBeaconRepository)
    assertTrue(result.isNotEmpty())
  }

  @Test
  fun placeBeaconRandomly_doesNotAddBeaconWhenCountIsEqualToThreshold() {
    val beacons = List(BEACON_COUNT) { Beacon("beacon$it", Location(46.519962, 6.633597)) }

    val result = placeBeaconsRandomly(beacons, location2, mockBeaconRepository)

    assertEquals(0, result.size)
  }

  @Test
  fun placeBeaconRandomly_doesAddBeacons() {
    val nbrMissing = 9
    val beacons =
        List(BEACON_COUNT - nbrMissing) { Beacon("beacon$it", Location(46.519962, 6.633597)) }

    val result = placeBeaconsRandomly(beacons, location2, mockBeaconRepository)

    assertEquals(nbrMissing, result.size)
  }

  @Test
  fun placeBeaconRandomly_selectsBeaconsWithMaxDistance() {
    val beacons = listOf<Beacon>()

    val result = placeBeaconsRandomly(beacons, location2, mockBeaconRepository)

    val maxDistance = computeDistanceBetweenBeacons(result.toMutableList(), beacons)
    result.forEachIndexed { index, beacon ->
      val otherBeacons = result.toMutableList().apply { removeAt(index) }
      val distance = computeDistanceBetweenBeacons(otherBeacons, beacons)
      assertTrue(distance <= maxDistance)
    }
  }

  @Test
  fun computeDistanceBetweenBeacons_returnsCorrectDistance() {
    val beacons =
        listOf(
            Beacon("beacon1", Location(46.519962, 6.633597)),
            Beacon("beacon2", Location(46.520962, 6.634597)))
    val newBeacons = mutableListOf(Beacon("beacon3", Location(46.521962, 6.635597)))

    val result = computeDistanceBetweenBeacons(newBeacons, beacons)

    assertTrue(result > 0)
  }

  @Test
  fun findRandomBeacon_addsBeaconToNewBeacons() {
    val newBeacons = mutableListOf<Beacon>()

    findRandomBeacon(location2, newBeacons, 0, mockBeaconRepository)
    assertEquals(1, newBeacons.size)
  }

  @Test
  fun haversine_returnsCorrectDistance() {
    val position1 = Location(46.519962, 6.633597)
    val position2 = Location(46.520962, 6.634597)

    val result = position1.distanceBetween(position2)

    assertTrue(result > 0)
  }

  @Test
  fun findNearbyBeacons_returnsOnlyBeaconsWithinRadius() {
    val userPosition = Location(46.519962, 6.633597)
    val beacons =
        listOf(
            Beacon("beacon1", Location(46.519962, 6.633597)), // within radius
            Beacon("beacon2", Location(46.520962, 6.634597)), // within radius
            Beacon("beacon3", Location(4.529962, 6.644597)) // outside radius
            )

    val result = findNearbyBeacons(userPosition, beacons)

    assertEquals(2, result.size)
  }

  @Test
  fun findNearbyBeacons_returnsEmptyListWhenNoBeaconsWithinRadius() {
    val userPosition = Location(46.519962, 6.633597)
    val beacons =
        listOf(
            Beacon("beacon1", Location(46.529962, 60.644597)), // outside radius
            Beacon("beacon2", Location(4.539962, 6.654597)) // outside radius
            )

    val result = findNearbyBeacons(userPosition, beacons)

    assertTrue(result.isEmpty())
  }

  @Test
  fun findClosestBeaconReturnsClosestBeacon() {
    val userPosition = Location(46.5196, 6.6323)
    val beacons =
        listOf(
            Beacon("Beacon1", Location(46.5197, 6.6324)),
            Beacon("Beacon2", Location(46.5198, 6.6325)),
            Beacon("Beacon3", Location(46.5199, 6.6326)))

    val closestBeacon = findClosestBeacon(userPosition, beacons)

    assertEquals("Beacon1", closestBeacon?.id)
  }

  @Test
  fun findNearbyBeacons_returnsAllBeaconsWhenAllWithinRadius() {
    val userPosition = Location(46.519962, 6.633597)
    val beacons =
        listOf(
            Beacon("beacon1", Location(46.519962, 6.633597)), // within radius
            Beacon("beacon2", Location(46.520962, 6.634597)) // within radius
            )

    val result = findNearbyBeacons(userPosition, beacons)

    assertEquals(beacons.size, result.size)
  }

  @Test
  fun randomLatLongFromPosition_returnsLocationWithinDistance() {
    val userPosition = Location(46.519962, 6.633597)
    val distance = 1000.0

    val result = randomLatLongFromPosition(userPosition, distance)

    val actualDistance = location2.distanceBetween(result)
    assertTrue(actualDistance <= distance)
  }

  @Test
  fun hasEnoughBeaconsReturnsTrueWhenBeaconCountIsMet() = runBlockingTest {
    val userPosition = Location(0.0, 0.0)
    val beacons = List(BEACON_COUNT) { Beacon("", Location(0.0, 0.0)) }

    val result = hasEnoughBeacons(userPosition, beacons)

    assertTrue(result)
  }

  @Test
  fun hasEnoughBeaconsReturnsFalseWhenBeaconCountIsNotMet() = runBlockingTest {
    val userPosition = Location(0.0, 0.0)
    val beacons = List(BEACON_COUNT - 1) { Beacon("", Location(0.0, 0.0)) }

    val result = hasEnoughBeacons(userPosition, beacons)

    assertFalse(result)
  }
}
