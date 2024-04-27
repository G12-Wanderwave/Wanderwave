package ch.epfl.cs311.wanderwave.utils

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.ui.components.utils.computeDistanceBetweenBeacons
import ch.epfl.cs311.wanderwave.ui.components.utils.findNearbyBeacons
import ch.epfl.cs311.wanderwave.ui.components.utils.findRandomBeacon
import ch.epfl.cs311.wanderwave.ui.components.utils.haversine
import ch.epfl.cs311.wanderwave.ui.components.utils.placeBeaconsRandomly
import ch.epfl.cs311.wanderwave.ui.components.utils.randomLatLongFromPosition
import com.google.android.gms.maps.model.LatLng
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

private const val BEACON_COUNT = 20

@RunWith(AndroidJUnit4::class)
class BeaconPlacerTest : TestCase() {

  @Test
  fun placeBeaconRandomly_addsBeaconWhenCountIsLessThanThreshold() {
    val location = LatLng(46.519962, 6.633597)
    val beacons = listOf<Beacon>()

    val result = placeBeaconsRandomly(beacons, location)
    assertTrue(result.isNotEmpty())
  }

  @Test
  fun placeBeaconRandomly_doesNotAddBeaconWhenCountIsEqualToThreshold() {
    val location = LatLng(46.519962, 6.633597)
    val beacons = List(BEACON_COUNT) { Beacon("beacon$it", Location(46.519962, 6.633597)) }

    val result = placeBeaconsRandomly(beacons, location)

    assertEquals(0, result.size)
  }

  @Test
  fun placeBeaconRandomly_doesAddBeacons() {
    val location = LatLng(46.519962, 6.633597)
    val nbrMissing = 9
    val beacons =
        List(BEACON_COUNT - nbrMissing) { Beacon("beacon$it", Location(46.519962, 6.633597)) }

    val result = placeBeaconsRandomly(beacons, location)

    assertEquals(nbrMissing, result.size)
  }

  @Test
  fun placeBeaconRandomly_selectsBeaconsWithMaxDistance() {
    val location = LatLng(46.519962, 6.633597)
    val beacons = listOf<Beacon>()

    val result = placeBeaconsRandomly(beacons, location)

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
    val location = LatLng(46.519962, 6.633597)
    val newBeacons = mutableListOf<Beacon>()

    findRandomBeacon(location, newBeacons, 0)
    Log.d("value of the random beacon ", newBeacons.toString())
    assertEquals(1, newBeacons.size)
  }

  @Test
  fun haversine_returnsCorrectDistance() {
    val position1 = LatLng(46.519962, 6.633597)
    val position2 = LatLng(46.520962, 6.634597)

    val result = haversine(position1, position2)

    assertTrue(result > 0)
  }

  @Test
  fun findNearbyBeacons_returnsOnlyBeaconsWithinRadius() {
    val userPosition = LatLng(46.519962, 6.633597)
    val beacons =
        listOf(
            Beacon("beacon1", Location(46.519962, 6.633597)), // within radius
            Beacon("beacon2", Location(46.520962, 6.634597)), // within radius
            Beacon("beacon3", Location(46.529962, 6.644597)) // outside radius
            )

    val result = findNearbyBeacons(userPosition, beacons, 1000.0)

    assertEquals(2, result.size)
  }

  @Test
  fun findNearbyBeacons_returnsEmptyListWhenNoBeaconsWithinRadius() {
    val userPosition = LatLng(46.519962, 6.633597)
    val beacons =
        listOf(
            Beacon("beacon1", Location(46.529962, 6.644597)), // outside radius
            Beacon("beacon2", Location(46.539962, 6.654597)) // outside radius
            )

    val result = findNearbyBeacons(userPosition, beacons, 1000.0)

    assertTrue(result.isEmpty())
  }

  @Test
  fun findNearbyBeacons_returnsAllBeaconsWhenAllWithinRadius() {
    val userPosition = LatLng(46.519962, 6.633597)
    val beacons =
        listOf(
            Beacon("beacon1", Location(46.519962, 6.633597)), // within radius
            Beacon("beacon2", Location(46.520962, 6.634597)) // within radius
            )

    val result = findNearbyBeacons(userPosition, beacons, 1000.0)

    assertEquals(beacons.size, result.size)
  }

  @Test
  fun randomLatLongFromPosition_returnsLocationWithinDistance() {
    val userPosition = LatLng(46.519962, 6.633597)
    val distance = 1000.0

    val result = randomLatLongFromPosition(userPosition, distance)

    val actualDistance = haversine(userPosition, result)
    Log.d("distance", "$actualDistance")
    assertTrue(actualDistance <= distance)
  }
}
