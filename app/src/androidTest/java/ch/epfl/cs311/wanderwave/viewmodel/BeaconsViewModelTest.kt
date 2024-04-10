package ch.epfl.cs311.wanderwave.ui

import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepositoryImpl
import ch.epfl.cs311.wanderwave.viewmodel.BeaconViewModel
import com.google.firebase.firestore.DocumentSnapshot
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
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

    val hashMap = beaconViewModel.beacon.value.toMap()
    // assert if the beacon is not null
    assert(beaconViewModel.beacon.value != null)
    // assert if the beacon is not null
    assert(hashMap != null)
  }
}
