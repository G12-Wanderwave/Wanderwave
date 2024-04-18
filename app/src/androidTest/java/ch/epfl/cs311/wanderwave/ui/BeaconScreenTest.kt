package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.junit4.createComposeRule
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.screens.BeaconScreen
import ch.epfl.cs311.wanderwave.viewmodel.BeaconViewModel
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BeaconScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockNavigationActions: NavigationActions
  private val viewModel = BeaconViewModel()

  @Before
  fun setup() {
    composeTestRule.setContent { BeaconScreen(mockNavigationActions, viewModel) }
  }

  @Test
  fun componentsAreDisplayed(): Unit = run {
    ComposeScreen.onComposeScreen<BeaconScreen>(composeTestRule) {
      assertIsDisplayed()

      beaconTitle { assertIsDisplayed() }
      beaconLocation { assertIsDisplayed() }
      beaconMap { assertIsDisplayed() }
      beaconTracksTitle { assertIsDisplayed() }
    }
  }
}
