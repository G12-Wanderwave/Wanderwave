package ch.epfl.cs311.wanderwave.ui

import android.util.Log
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.local.BeaconEntity
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepositoryImpl
import ch.epfl.cs311.wanderwave.viewmodel.BeaconViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.DocumentSnapshot
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import junit.framework.TestCase.assertEquals
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BeaconConnectionTest {
  @get:Rule val mockkRule = MockKRule(this)
  private lateinit var beaconConnection: BeaconConnection
  private lateinit var beaconViewModel: BeaconViewModel
  @RelaxedMockK private lateinit var repository: ProfileRepositoryImpl

  @RelaxedMockK private lateinit var document: DocumentSnapshot

  @Before
  fun setup() {
    beaconViewModel = BeaconViewModel(repository)
    beaconConnection = beaconViewModel.beaconConnection

    MockKAnnotations.init(this)

    // Set up the document mock to return some tracks
    every { document.id } returns "someId"
    every { document["title"] } returns "someTitle"
    every { document["artist"] } returns "someArtist"
    // Set up the documentTrack mock to return some beacons
    every { document.exists() } returns true
    every { document.get("location") } returns
        mapOf("latitude" to 1.0, "longitude" to 1.0, "name" to "Test Location")
    every { document.getString("firstName") } returns "TestFirstName"
    every { document.getString("lastName") } returns "TestLastName"
    every { document.getString("description") } returns "TestDescription"
    every { document.getLong("numberOfLikes") } returns 10L
    every { document.getBoolean("isPublic") } returns true
    every { document.getString("profilePictureUri") } returns "https://example.com/profile.jpg"
    every { document.getString("spotifyUid") } returns "TestSpotifyUid"
    every { document.getString("firebaseUid") } returns "TestFirebaseUid"

  }

  @Test
  fun testViewModelBasicsVariables() {
    // assert if the beacon is not null
    assert(beaconViewModel.beacon != null)
    // assert if the beaconConnection is not null
    assert(beaconViewModel.beaconConnection != null)

    val hashMap = beaconViewModel.beacon.value.toHashMap()
    // assert if the beacon is not null
    assert(beaconViewModel.beacon.value != null)
    // assert if the beacon is not null
    assert(hashMap != null)
  }

  @Test
  fun documentSnapshotToBeacon() {
    val beacon = Beacon.from(document)
    // assert if the beacon is not null
    assert(beacon != null)

    every { document.exists() } returns false

    val beacon2 = Beacon.from(document)

    // assert if the beacon is null
    assert(beacon2 == null)
  }

  @Test
  fun documentSnapshotToProfile() {
    val profile = Profile.from(document)
    // assert if the profile is not null
    assert(profile != null)

    every { document.exists() } returns false

    val profile2 = Profile.from(document)

    // assert if the profile is null
    assert(profile2 == null)
  }

  @Test
  fun DocumentSnapshotToTrack() {
    val track = Track.from(document)
    // assert if the track is not null
    assert(track != null)

    every { document.exists() } returns false

    val track2 = Track.from(document)

    // assert if the track is null
    assert(track2 == null)
  }


  @Test
  fun basicVariablesLocation() {
    val location = Location(1.0, 1.0, "Test Location")
    val latLng: LatLng = location.toLatLng()
    val hashMap: HashMap<String, Any> = location.toHashMap()
    // test location behaviour
    assertEquals(1.0, location.latitude)
    assertEquals(1.0, location.longitude)
    assertEquals("Test Location", location.name)
    assertEquals(1.0, latLng.latitude)
    assertEquals(1.0, latLng.longitude)
    assertEquals(1.0, hashMap["latitude"])
    assertEquals(1.0, hashMap["longitude"])
    assertEquals("Test Location", hashMap["name"])
  }

  @Test
  fun beaconEntityIsCorrectlyInitialized() {
    val id = "testId"
    val latitude = 1.0
    val longitude = 1.0

    val beaconEntity = BeaconEntity(id, latitude, longitude)

    assertEquals(id, beaconEntity.id)
    assertEquals(latitude, beaconEntity.latitude, 0.0)
    assertEquals(longitude, beaconEntity.longitude, 0.0)
  }

  @Test
  fun testAddAndGetItem() = runBlocking {
    withTimeout(30000) { // Timeout after 5000 milliseconds (20 seconds)
      val beacon =
          Beacon(
              id = "testBeacon",
              location = Location(1.0, 1.0, "Test Location"),
              tracks = listOf(Track("testTrack", "Test Title", "Test Artist")))

      beaconConnection.addItemWithId(beacon)

      val retrievedBeacon = beaconConnection.getItem("testBeacon").first()
      Log.d("Firestore", "$retrievedBeacon $beacon")
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
