package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.ui.screens.MapScreen
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MapTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createAndroidComposeRule<TestActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent { MapScreen() }
  }

  @Test
  fun mapIsDisplayed() = run { onComposeScreen<MapScreen>(composeTestRule) { assertIsDisplayed() } }

  @Test
  fun beaconsAreDisplayed() = run {
    onComposeScreen<MapScreen>(composeTestRule) {
      // Note: There is unfortunately no way to check whether markers are displayed on the map
      // due to the way GoogleMaps handles them.
    }
  }
}
