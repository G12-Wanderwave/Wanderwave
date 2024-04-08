package ch.epfl.cs311.wanderwave.ui

import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Before
import org.junit.Test

class BeaconConnectionTest {

  private lateinit var beaconConnection: BeaconConnection

  @Before
  fun setup() {
    beaconConnection = BeaconConnection()
  }

  @Test
  fun testAddAndGetItem() = runBlocking {
    withTimeout(20000) { // Timeout after 5000 milliseconds (20 seconds)
      val beacon =
          Beacon(
              id = "testBeacon",
              location = Location(1.0, 1.0, "Test Location"),
              tracks = listOf(Track("testTrack", "Test Title", "Test Artist")))

      beaconConnection.addItemWithId(beacon)

      Log.d("Firestore", "Added item")
      val retrievedBeacon = beaconConnection.getItem("testBeacon").first()

      assert(beacon == retrievedBeacon)
    }
  }

  // If someone knows how to deal with the flow already being null and how to test it, please let me
  // know
  @Test
  fun testGetNonexistentItem() = runBlocking {
    // By default the test passes, if a value is emitted the test fails
    withTimeout(20000) {
      // Flag to indicate if the flow emits any value
      var valueEmitted = false

      // Collect the flow within a 2-second timeout
      measureTimeMillis {
        withTimeoutOrNull(2000) {
          beaconConnection.getItem("nonexistentBeacon").collect {
            valueEmitted = true // Set the flag if the flow emits any value
          }
        }
      }

      // Assert that the flow didn't emit anything within the timeout
      assert(valueEmitted.not()) { "Flow emitted unexpected value" }
    }
  }

  @Test
  fun testAddItemTwice() = runBlocking {
    withTimeout(20000) {
      val beacon =
          Beacon(
              id = "testBeacon",
              location = Location(1.0, 1.0, "Test Location"),
              tracks = listOf(Track("testTrack", "Test Title", "Test Artist")))

      beaconConnection.addItemWithId(beacon)
      beaconConnection.addItemWithId(beacon)

      val retrievedBeacon = beaconConnection.getItem("testBeacon").first()

      assert(beacon == retrievedBeacon)
    }
  }

  @Test
  fun AddDeleteAndGetItem() = runBlocking {
    withTimeout(20000) {
      val beacon =
          Beacon(
              id = "testBeacon1",
              location = Location(1.0, 1.0, "Test Location"),
              tracks = listOf(Track("testTrack", "Test Title", "Test Artist")))

      beaconConnection.addItemWithId(beacon)
      beaconConnection.deleteItem("testBeacon1")

      // Flag to indicate if the flow emits any value
      var valueEmitted = false

      // Collect the flow within a 2-second timeout
      measureTimeMillis {
        withTimeoutOrNull(2000) {
          beaconConnection.getItem("testBeacon1").collect {
            valueEmitted = true // Set the flag if the flow emits any value
          }
        }
      }

      // Assert that the flow didn't emit anything within the timeout
      assert(valueEmitted.not()) { "Flow emitted unexpected value" }
    }
  }

  @After
  fun cleanupTestData() = runBlocking {
    // Remove the test data
    beaconConnection.deleteItem("testBeacon")
    beaconConnection.deleteItem("testBeacon1")
    beaconConnection.deleteItem("testBeacon2")
    beaconConnection.deleteItem("nonexistentBeacon")
  }
}