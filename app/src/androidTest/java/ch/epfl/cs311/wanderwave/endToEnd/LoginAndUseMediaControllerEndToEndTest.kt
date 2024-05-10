package ch.epfl.cs311.wanderwave.endToEnd

import android.Manifest
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import ch.epfl.cs311.wanderwave.MainActivity
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.ui.screens.AppScreen
import ch.epfl.cs311.wanderwave.ui.screens.LoginScreen
import ch.epfl.cs311.wanderwave.ui.screens.SpotifyConnectScreen
import ch.epfl.cs311.wanderwave.ui.screens.TrackListScreen
import ch.epfl.cs311.wanderwave.ui.screens.components.ExclusivePlayerScreen
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginAndUseMediaControllerEndToEndTest :
    TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockSpotifyController: SpotifyController
  @RelaxedMockK private lateinit var mockAuthenticationController: AuthenticationController

  @Before
  fun setup() {
    every { mockSpotifyController.connectRemote() } returns
        flowOf(SpotifyController.ConnectResult.SUCCESS)
    every { mockAuthenticationController.isSignedIn() } returns true
  }

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

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
