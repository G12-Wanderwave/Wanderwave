package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.ui.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.screens.LoginScreen
import ch.epfl.cs311.wanderwave.viewmodel.LoginScreenViewModel
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {
  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockNavigationActions: NavigationActions

  @RelaxedMockK private lateinit var mockViewModel: LoginScreenViewModel

  @Before
  fun setup() {
    every { mockViewModel.uiState } returns
        MutableStateFlow(LoginScreenViewModel.LoginScreenUiState())
    composeTestRule.setContent { LoginScreen(mockNavigationActions, {}, mockViewModel) }
  }

  @Test
  fun loginScreenComponentsAreDisplayedAndButtonIsClickable() = run {
    onComposeScreen<LoginScreen>(composeTestRule) {
      assertIsDisplayed()
      appLogo { assertIsDisplayed() }
      welcomeTitle {
        assertIsDisplayed()
        hasText("Welcome")
      }
      welcomeSubtitle {
        assertIsDisplayed()
        hasText("Ready to discover new music?")
      }
      signInButton {
        assertIsDisplayed()
        hasText("Sign in with Spotify")
        performClick()
        // TODO verify that the viewModel method is called
      }
    }
  }
}
