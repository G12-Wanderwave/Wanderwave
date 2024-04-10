package ch.epfl.cs311.wanderwave.model

import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.DocumentSnapshot
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DataClassesTest {

  @get:Rule val mockkRule = MockKRule(this)
  private lateinit var beaconConnection: BeaconConnection

  @RelaxedMockK private lateinit var document: DocumentSnapshot

  @Before
  fun setup() {
    beaconConnection = BeaconConnection()

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
  fun documentSnapshotToProfile() {
    every { document.exists() } returns true
    val profile = Profile.from(document)
    // assert if the profile is not null
    assert(profile != null)

    every { document.exists() } returns false

    val profile2 = Profile.from(document)

    // assert if the profile is null
    assert(profile2 == null)
  }

  @Test
  fun documentSnapshotToBeacon() {
    every { document.exists() } returns true

    val beacon = Beacon.from(document)
    // assert if the beacon is not null
    assert(beacon != null)

    every { document.exists() } returns false

    val beacon2 = Beacon.from(document)

    // assert if the beacon is null
    assert(beacon2 == null)
  }

  @Test
  fun basicVariablesLocation() {
    val location = Location(1.0, 1.0, "Test Location")
    val latLng: LatLng = location.toLatLng()
    val locationMap: Map<String, Any> = location.toMap()
    // test location behaviour
    assertEquals(1.0, location.latitude)
    assertEquals(1.0, location.longitude)
    assertEquals("Test Location", location.name)
    assertEquals(1.0, latLng.latitude)
    assertEquals(1.0, latLng.longitude)
    assertEquals(1.0, locationMap["latitude"])
    assertEquals(1.0, locationMap["longitude"])
    assertEquals("Test Location", locationMap["name"])
  }

  @Test
  fun DocumentSnapshotToTrack() {
    every { document.exists() } returns true
    val track = Track.from(document)
    // assert if the track is not null
    assert(track != null)

    every { document.exists() } returns false

    val track2 = Track.from(document)

    // assert if the track is null
    assert(track2 == null)
  }
}
