package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.MainActivity
import ch.epfl.cs311.wanderwave.ui.screens.AppBottomBarScreen
import ch.epfl.cs311.wanderwave.ui.screens.AppScreen
import ch.epfl.cs311.wanderwave.ui.screens.LoginScreen
import ch.epfl.cs311.wanderwave.ui.screens.MainPlaceHolder
import ch.epfl.cs311.wanderwave.ui.screens.MapScreen
import ch.epfl.cs311.wanderwave.ui.screens.ProfileScreen
import ch.epfl.cs311.wanderwave.ui.screens.TrackListScreen
import ch.epfl.cs311.wanderwave.ui.screens.components.MiniPlayerScreen
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun appIsDisplayed() = run { onComposeScreen<AppScreen>(composeTestRule) { assertIsDisplayed() } }

  @Test
  fun canNavigateFromLoginToMainToTrackListThenInteractWithMiniPlayer() = run {
    onComposeScreen<LoginScreen>(composeTestRule) { signInButton.performClick() }
    onComposeScreen<MainPlaceHolder>(composeTestRule) { assertIsDisplayed() }
    onComposeScreen<AppBottomBarScreen>(composeTestRule) {
      bottomAppBarTrackListButton.performClick()
    }
    onComposeScreen<TrackListScreen>(composeTestRule) { assertIsDisplayed() }
    onComposeScreen<MiniPlayerScreen>(composeTestRule) {
      assertIsDisplayed()
      playPauseButton.performClick()
      playPauseButton.performClick()
      miniPlayerTitleButton.performClick()
    }
  }

  @Test
  fun canNavigateToMapScreen() = run {
    onComposeScreen<LoginScreen>(composeTestRule) { signInButton.performClick() }
    onComposeScreen<MainPlaceHolder>(composeTestRule) { assertIsDisplayed() }
    onComposeScreen<AppBottomBarScreen>(composeTestRule) { mapScreenButton.performClick() }
    onComposeScreen<MapScreen>(composeTestRule) { assertIsDisplayed() }
  }

  @Test
  fun canNavigateToProfileScreen() = run {
    onComposeScreen<LoginScreen>(composeTestRule) { signInButton.performClick() }
    onComposeScreen<MainPlaceHolder>(composeTestRule) {
      assertIsDisplayed()
      profileButton.performClick()
    }
    onComposeScreen<ProfileScreen>(composeTestRule) { assertIsDisplayed() }
  }
}
