package ch.epfl.cs311.wanderwave.ui

import android.app.Instrumentation
import android.content.Intent
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.screens.LogoutScreen
import ch.epfl.cs311.wanderwave.viewmodel.LogoutScreenViewModel
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogoutScreenTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {
  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val mockkRule = MockKRule(this)

  @get:Rule val intentsRule = IntentsRule()

  @RelaxedMockK private lateinit var mockNavigationActions: NavigationActions

  @RelaxedMockK private lateinit var mockViewModel: LogoutScreenViewModel

  @RelaxedMockK private lateinit var mockShowMessage: (String) -> Unit

  fun setup(uiState: LogoutScreenViewModel.UiState = LogoutScreenViewModel.UiState()) {
    every { mockViewModel.uiState } returns MutableStateFlow(uiState)
    every { mockViewModel.getAuthorizationRequest() } returns
        AuthorizationRequest.Builder(
                "clientid", AuthorizationResponse.Type.TOKEN, "fake-scheme://callback")
            .build()
    every { mockViewModel.handleAuthorizationResponse(any()) } returns Unit
    composeTestRule.setContent {
      LogoutScreen(mockNavigationActions, mockShowMessage, mockViewModel)
    }
  }

  @Test
  fun logoutScreenComponentsAreDisplayed() = run {
    setup()
    onComposeScreen<LogoutScreen>(composeTestRule) {
      assertIsDisplayed()
      logoutProgressIndicator { assertIsDisplayed() }
    }
  }

  @Test
  fun spotifyLogoutRunsIntent() = run {
    setup()
    val responseDummyIntent = Intent("responseDummy")
    val result = Instrumentation.ActivityResult(123, responseDummyIntent)
    Intents.intending(anyIntent()).respondWith(result)

    onComposeScreen<LogoutScreen>(composeTestRule) { assertIsDisplayed() }
    verify { mockViewModel.getAuthorizationRequest() }

    verify { mockViewModel.handleAuthorizationResponse(any()) }
  }

  @Test
  fun logoutSuccessNavigatesToLogin() = run {
    setup(LogoutScreenViewModel.UiState(hasResult = true, success = true))
    onComposeScreen<LogoutScreen>(composeTestRule) {
      assertIsDisplayed()

      verify { mockNavigationActions.navigateTo(Route.LOGIN) }
    }
  }

  @Test
  fun loginFailureShowsErrorMessage() = run {
    val errorMessage = "Error logging in"
    setup(LogoutScreenViewModel.UiState(hasResult = true, success = false, message = errorMessage))
    onComposeScreen<LogoutScreen>(composeTestRule) {
      assertIsDisplayed()
      verify { mockShowMessage(errorMessage) }
    }
  }
}
