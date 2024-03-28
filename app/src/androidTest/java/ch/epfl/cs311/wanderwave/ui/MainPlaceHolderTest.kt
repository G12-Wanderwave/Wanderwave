package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.screens.MainPlaceHolder
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
class MainPlaceHolderTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {
  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockNavigationActions: NavigationActions

  @Before
  fun setup() {
    composeTestRule.setContent { MainPlaceHolder(mockNavigationActions) }
  }

  @Test
  fun mainPlaceHolderIsDisplayedAndSignOutButtonClickNavigatesToLogout() = run {
    onComposeScreen<MainPlaceHolder>(composeTestRule) {
      assertIsDisplayed()
      signOutButton {
        assertIsDisplayed()
        performClick()
      }
    }
    // Verify that navigateTo is called with the correct argument
    verify { mockNavigationActions.navigateTo(Route.LOGOUT) }
  }
}
