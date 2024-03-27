package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.ui.components.AppBottomBar
import ch.epfl.cs311.wanderwave.ui.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.navigation.Route
import ch.epfl.cs311.wanderwave.ui.screens.AppBottomBarScreen
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppBottomBarTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {
  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockNavigationActions: NavigationActions

  @Before
  fun setup() {
    composeTestRule.setContent { AppBottomBar(mockNavigationActions) }
  }

  @Test
  fun appBottomBarComponentsAreDisplayedAndButtonIsClickable() = run {
    onComposeScreen<AppBottomBarScreen>(composeTestRule) {
      assertIsDisplayed()

      bottomAppBarMainPlaceHolderButton {
        assertIsDisplayed()
        performClick()
        verify { mockNavigationActions.navigateToTopLevel(Route.MAIN) }
      }
      bottomAppBarTrackListButton {
        assertIsDisplayed()
        performClick()
        verify { mockNavigationActions.navigateToTopLevel(Route.TRACK_LIST) }
      }
    }
  }
}
