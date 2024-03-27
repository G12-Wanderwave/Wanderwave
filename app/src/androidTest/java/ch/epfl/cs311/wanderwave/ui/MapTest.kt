package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.model.data.Beacon
import ch.epfl.cs311.wanderwave.model.data.Location
import ch.epfl.cs311.wanderwave.ui.screens.MapScreen
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import io.mockk.junit4.MockKRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {
  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val mockkRule = MockKRule(this)

  @Before
  fun setup() {
    composeTestRule.setContent { MapScreen() }
  }

  @Test
  fun mapIsDisplayed() = run {
    onComposeScreen<MapScreen>(composeTestRule) {
      assertIsDisplayed()
      marker.assertIsNotDisplayed()
    }
  }

  @Test
  fun beaconsAreDisplayed() = run {
    onComposeScreen<MapScreen>(composeTestRule) {
      val beacons =
          listOf(
              Beacon("1", Location(46.51857556996283, 6.5631609607190775), emptyList()),
              Beacon("2", Location(46.51857417773428, 6.5619195033506434), emptyList()),
              Beacon("3", Location(46.52298529087412, 6.564644391110982), emptyList()),
              Beacon("4", Location(46.51846723837138, 6.568149323030634), emptyList()))
      marker.assertIsDisplayed()
    }
  }
}
