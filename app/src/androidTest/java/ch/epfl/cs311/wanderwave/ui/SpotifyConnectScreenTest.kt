package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.screens.SpotifyConnectScreen
import ch.epfl.cs311.wanderwave.viewmodel.SpotifyConnectScreenViewModel
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpotifyConnectScreenTest :
    TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockNavigationActions: NavigationActions

  @RelaxedMockK private lateinit var mockViewModel: SpotifyConnectScreenViewModel

  private val uiStateFlow = MutableStateFlow(SpotifyConnectScreenViewModel.UiState())
  private val isFirstTimeFlow = MutableStateFlow(false)

  private fun setup(uiState: SpotifyConnectScreenViewModel.UiState, isFirstTime: Boolean) {
    every { mockViewModel.uiState } returns uiStateFlow
    every { mockViewModel.isFirstTime } returns isFirstTimeFlow

    uiStateFlow.value = uiState
    isFirstTimeFlow.value = isFirstTime

    composeTestRule.setContent {
      SpotifyConnectScreen(navigationActions = mockNavigationActions, viewModel = mockViewModel)
    }
  }

  @Test
  fun spotifyConnectScreenProgressIndicatorIsDisplayed() = run {
    setup(SpotifyConnectScreenViewModel.UiState(hasResult = false), isFirstTime = false)

    onComposeScreen<SpotifyConnectScreen>(composeTestRule) { assertIsDisplayed() }
  }

  @Test
  fun spotifyConnectScreenTriesToConnect() = run {
    setup(SpotifyConnectScreenViewModel.UiState(hasResult = false), isFirstTime = false)

    onComposeScreen<SpotifyConnectScreen>(composeTestRule) {}
    coVerify { mockViewModel.connectRemote() }
  }

  @Test
  fun spotifyConnectScreenNavigatesToMapOnSuccess() = run {
    setup(
        SpotifyConnectScreenViewModel.UiState(hasResult = true, success = true),
        isFirstTime = false)

    onComposeScreen<SpotifyConnectScreen>(composeTestRule) {}
    coVerify { mockNavigationActions.navigateToTopLevel(Route.MAP) }
  }

  @Test
  fun spotifyConnectScreenNavigatesToLoginOnFailure() = run {
    setup(
        SpotifyConnectScreenViewModel.UiState(hasResult = true, success = false),
        isFirstTime = false)

    onComposeScreen<SpotifyConnectScreen>(composeTestRule) {}
    coVerify { mockNavigationActions.navigateToTopLevel(Route.LOGIN) }
  }
}
