package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.junit4.createComposeRule
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.remote.BeaconConnection
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.screens.BeaconScreen
import ch.epfl.cs311.wanderwave.viewmodel.BeaconViewModel
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BeaconScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockNavigationActions: NavigationActions
  @RelaxedMockK private lateinit var beaconConnection: BeaconConnection

  @Before
  fun setup() {

    val beaconFlow =
        flowOf(
            Beacon(
                "UAn8OUadgrUOKYagf8a2",
                Location(46.519653, 6.632273, "Lausanne"),
                listOf<Track>(Track("Some Track ID", "Track Title", "Artist Name")),
            ))
    coEvery { beaconConnection.getItem(any<Beacon>()) } returns beaconFlow
    coEvery { beaconConnection.getItem(any<String>()) } returns beaconFlow

    val viewModel = BeaconViewModel(beaconConnection)

    composeTestRule.setContent { BeaconScreen(mockNavigationActions, viewModel) }
  }

  @Test
  fun componentsAreDisplayed(): Unit = runBlockingTest {
    ComposeScreen.onComposeScreen<BeaconScreen>(composeTestRule) {
      assertIsDisplayed()

      beaconTitle { assertIsDisplayed() }
      beaconLocation { assertIsDisplayed() }
      beaconMap { assertIsDisplayed() }
      beaconTracksTitle { assertIsDisplayed() }
    }
  }
}
