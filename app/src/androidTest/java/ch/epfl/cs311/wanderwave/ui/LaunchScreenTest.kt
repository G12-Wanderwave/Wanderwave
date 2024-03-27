package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.junit4.createComposeRule
import ch.epfl.cs311.wanderwave.ui.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.navigation.Route
import ch.epfl.cs311.wanderwave.ui.screens.LaunchScreen
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LaunchScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @RelaxedMockK lateinit var mockNavigationActions: NavigationActions

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun launchScreenNavigatesToLogin() {
    composeTestRule.setContent { LaunchScreen(mockNavigationActions) }
    // Verify that navigateTo is called with the correct argument
    verify { mockNavigationActions.navigateToTopLevel(Route.LOGIN) }
  }
}
