package ch.epfl.cs311.wanderwave.ui

import android.app.Instrumentation
import android.content.Intent
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.screens.LoginScreen
import ch.epfl.cs311.wanderwave.viewmodel.LoginScreenViewModel
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {
  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val mockkRule = MockKRule(this)

  @get:Rule val intentsRule = IntentsRule()

  @RelaxedMockK private lateinit var mockNavigationActions: NavigationActions

  @RelaxedMockK private lateinit var mockViewModel: LoginScreenViewModel

  @RelaxedMockK private lateinit var mockShowMessage: (String) -> Unit

  fun setup(uiState: LoginScreenViewModel.UiState = LoginScreenViewModel.UiState()) {
    every { mockViewModel.uiState } returns MutableStateFlow(uiState)
    every { mockViewModel.getAuthorizationRequest() } returns
        AuthorizationRequest.Builder(
                "clientid", AuthorizationResponse.Type.TOKEN, "fake-scheme://callback")
            .build()
    every { mockViewModel.handleAuthorizationResponse(any()) } returns Unit
    composeTestRule.setContent {
      LoginScreen(mockNavigationActions, mockShowMessage, mockViewModel)
    }
  }

  @Test
  fun loginScreenComponentsAreDisplayedAndButtonIsClickable() = run {
    setup()
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
        hasText("Sign in with Spotify") // TODO don't hardcode strings
        assertHasClickAction()
      }
    }
  }

  @Test
  fun spotifyLoginRunsIntent() = run {
    setup()
    val responseDummyIntent = Intent("responseDummy")
    val result = Instrumentation.ActivityResult(123, responseDummyIntent)
    Intents.intending(anyIntent()).respondWith(result)

    mockkStatic(AuthorizationClient::class)
    every { AuthorizationClient.createLoginActivityIntent(any(), any()) } returns
        mockk(relaxed = true)

    onComposeScreen<LoginScreen>(composeTestRule) {
      signInButton { performClick() }
      verify { mockViewModel.getAuthorizationRequest() }

      Intents.intended(anyIntent())
      Intents.assertNoUnverifiedIntents()

      verify { mockViewModel.handleAuthorizationResponse(any()) }
    }
  }

  @Test
  fun loginSuccessNavigatesToSpotifyConnect() = run {
    setup(LoginScreenViewModel.UiState(hasResult = true, success = true))
    onComposeScreen<LoginScreen>(composeTestRule) {
      assertIsDisplayed()

      verify { mockNavigationActions.navigateTo(Route.SPOTIFY_CONNECT) }
    }
  }

  @Test
  fun loginFailureShowsErrorMessage() = run {
    val errorMessage = "Error logging in"
    setup(LoginScreenViewModel.UiState(hasResult = true, success = false, message = errorMessage))
    onComposeScreen<LoginScreen>(composeTestRule) {
      assertIsDisplayed()
      verify { mockShowMessage(errorMessage) }
    }
  }
}
