package ch.epfl.cs311.wanderwave.viewmodel

import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class BeaconScreenViewModelTest {

  @get:Rule val mockkRule = MockKRule(this)
  @RelaxedMockK private lateinit var beaconConnection: BeaconConnection

  @RelaxedMockK private lateinit var mockSpotifyController: SpotifyController

  @Test
  fun canConstructWithNoErrors() {
    BeaconViewModel(beaconConnection, mockSpotifyController)
  }

  @Test
  fun addTrackToBeaconTest() {
    val viewModel = BeaconViewModel(beaconConnection, mockSpotifyController)
    val track = Track("trackId", "trackName", "trackArtist")
    viewModel.addTrackToBeacon("beaconId", track, {})

    verify { beaconConnection.addTrackToBeacon("beaconId", track, any()) }
  }
}
