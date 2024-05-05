package ch.epfl.cs311.wanderwave.endToEnd

import android.Manifest
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import ch.epfl.cs311.wanderwave.MainActivity
import ch.epfl.cs311.wanderwave.ui.screens.AppScreen
import ch.epfl.cs311.wanderwave.ui.screens.LoginScreen
import ch.epfl.cs311.wanderwave.ui.screens.SpotifyConnectScreen
import ch.epfl.cs311.wanderwave.ui.screens.TrackListScreen
import ch.epfl.cs311.wanderwave.ui.screens.components.ExclusivePlayerScreen
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class LoginAndUseMediaControllerEndToEndTest :
    TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @get:Rule val mockkRule = MockKRule(this)

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_COARSE_LOCATION,
          Manifest.permission.ACCESS_FINE_LOCATION,
      )

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun testEndToEnd() = runBlockingTest {
    ComposeScreen.onComposeScreen<LoginScreen>(composeTestRule) {
      assertIsDisplayed()
      signInButton.assertIsDisplayed()
      signInButton.performClick()
    }

    ComposeScreen.onComposeScreen<SpotifyConnectScreen>(composeTestRule) { assertIsDisplayed() }

    ComposeScreen.onComposeScreen<AppScreen>(composeTestRule) {
      assertIsDisplayed()
      appScaffold.assertIsDisplayed()
      appBottomBar.assertIsDisplayed()

      trackListButton.assertIsDisplayed()
      trackListButton.performClick()
    }

    ComposeScreen.onComposeScreen<TrackListScreen>(composeTestRule) {
      assertIsDisplayed()
      trackButton.assertIsDisplayed()
      searchBar.assertIsDisplayed()

      trackButton.performClick()
    }

    ComposeScreen.onComposeScreen<AppScreen>(composeTestRule) {
      miniPlayer.assertIsDisplayed()

      miniPlayerPlayButton.assertIsDisplayed()
      miniPlayerPlayButton.performClick()

      miniPlayerTitle.assertIsDisplayed()
      miniPlayerTitle.performClick()
    }

    ComposeScreen.onComposeScreen<ExclusivePlayerScreen>(composeTestRule) {
      assertIsDisplayed()

      playerControl.assertIsDisplayed()

      trackInfo.assertIsDisplayed()
      artist.assertIsDisplayed()
      artist.assertTextContains("1")
      title.assertIsDisplayed()
      title.assertTextContains("1")

      playPauseButton.assertIsDisplayed()
      playPauseButton.performClick()

      nextButton.assertIsDisplayed()
      nextButton.performClick()
      artist.assertTextContains("Percival Schuttenbach")
      title.assertIsDisplayed()
      title.assertTextContains("The Nightingale")

      previousButton.assertIsDisplayed()
      previousButton.performClick()
      artist.assertTextContains("1")
      title.assertIsDisplayed()
      title.assertTextContains("1")

      shuffleButton.assertIsDisplayed()
      shuffleButton.performClick()

      loopButton.assertIsDisplayed()
      loopButton.performClick()
      loopButton.performClick()
    }
  }
}

/*
package ch.epfl.cs311.wanderwave.endToEnd

import android.util.Log
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.TrackRepositoryImpl
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.App
import ch.epfl.cs311.wanderwave.ui.TestActivity
import ch.epfl.cs311.wanderwave.ui.screens.AppScreen
import ch.epfl.cs311.wanderwave.ui.screens.LoginScreen
import ch.epfl.cs311.wanderwave.ui.screens.SpotifyConnectScreen
import ch.epfl.cs311.wanderwave.ui.screens.TrackListScreen
import ch.epfl.cs311.wanderwave.viewmodel.LoginScreenViewModel
import ch.epfl.cs311.wanderwave.viewmodel.SpotifyConnectScreenViewModel
import ch.epfl.cs311.wanderwave.viewmodel.TrackListViewModel
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EndToEndTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createAndroidComposeRule<TestActivity>()

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockNavController: NavHostController

  @RelaxedMockK private lateinit var mockNavigationActions: NavigationActions

  @RelaxedMockK
  private lateinit var mockSpotifyConnectScreenViewModel: SpotifyConnectScreenViewModel

  @RelaxedMockK private lateinit var mockTrackListViewModel: TrackListViewModel

  @RelaxedMockK private lateinit var mockSpotifyController: SpotifyController

  @RelaxedMockK private lateinit var mockRepositoryImpl: TrackRepositoryImpl

  @RelaxedMockK private lateinit var mockLoginScreenViewModel: LoginScreenViewModel

  @After
  fun clearMocks() {
    clearAllMocks() // Clear all MockK mocks
  }

  @Before
  fun setup() {
    mockNavController = mockk()
    every { mockNavController.navigate(any<String>()) } just Runs
    every { mockNavController.currentBackStackEntry } returns
            mockk { every { destination } returns mockk { every { route } returns "expected_route" } }

    val connectResult = SpotifyController.ConnectResult.SUCCESS
    every { mockSpotifyController.connectRemote() } returns flowOf(connectResult)

    val track1 = Track("spotify:track:6ImuyUQYhJKEKFtlrstHCD", "Main Title", "John Williams")
    val track2 =
      Track("spotify:track:0HLQFjnwq0FHpNVxormx60", "The Nightingale", "Percival Schuttenbach")
    val track3 =
      Track("spotify:track:2NZhNbfb1rD1aRj3hZaoqk", "The Imperial Suite", "Michael Giacchino")
    val track4 = Track("spotify:track:5EWPGh7jbTNO2wakv8LjUI", "Free Bird", "Lynyrd Skynyrd")
    val track5 = Track("spotify:track:4rTlPsga6T8yiHGOvZAPhJ", "Godzilla", "Eminem")

    val trackList =
      listOf(
        track1,
        track2,
        track3,
        track4,
        track5,
      )

    mockRepositoryImpl = mockk()
    every { mockRepositoryImpl.getAll() } returns flowOf(trackList)

    val _mockUIState =
      MutableStateFlow(SpotifyConnectScreenViewModel.UiState(hasResult = true, success = true))
    val mockUIState: StateFlow<SpotifyConnectScreenViewModel.UiState> = _mockUIState
    mockSpotifyConnectScreenViewModel = mockk()
    every { mockSpotifyConnectScreenViewModel.uiState } returns mockUIState

    mockTrackListViewModel =
      TrackListViewModel(
        spotifyController = mockSpotifyController, repository = mockRepositoryImpl)

    mockLoginScreenViewModel = LoginScreenViewModel(spotifyController = mockSpotifyController)

    composeTestRule.setContent {
      LoginScreen(
        navigationActions = mockNavigationActions,
        showMessage = { m: String -> Log.d("LoginScreen", m) },
        viewModel = mockLoginScreenViewModel)

      SpotifyConnectScreen(
        navigationActions = mockNavigationActions, viewModel = mockSpotifyConnectScreenViewModel)

      App(navController = mockNavController)

      TrackListScreen(
        showMessage = { m -> Log.d("TrackListScreen", m) }, viewModel = mockTrackListViewModel)
    }
  }

  @Test
  fun testEndToEnd() = run {
    runTest {
      onComposeScreen<LoginScreen>(composeTestRule) {
        assertIsDisplayed()
        signInButton.assertIsDisplayed()
        signInButton.performClick()
      }

      onComposeScreen<SpotifyConnectScreen>(composeTestRule) { assertIsDisplayed() }

      onComposeScreen<AppScreen>(composeTestRule) {
        assertIsDisplayed()
        appBottomBarScreen.assertIsDisplayed()
        trackListButton.assertIsDisplayed()
        trackListButton.performClick()
      }

      onComposeScreen<TrackListScreen>(composeTestRule) { assertIsDisplayed() }
    }
  }
}
*/
