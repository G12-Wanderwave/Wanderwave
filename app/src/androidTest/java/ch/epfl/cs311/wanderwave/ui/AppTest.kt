package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.MainActivity
import ch.epfl.cs311.wanderwave.ui.screens.AppBottomBarScreen
import ch.epfl.cs311.wanderwave.ui.screens.AppScreen
import ch.epfl.cs311.wanderwave.ui.screens.LoginScreen
import ch.epfl.cs311.wanderwave.ui.screens.MainPlaceHolder
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
  fun canNavigateFromSignInToMainAndInteractWithBottomNavigation() = run {
    val loginScreen = LoginScreen(composeTestRule)

    // Wait for the sign-in screen to be displayed
    composeTestRule.waitUntil {
      try {
        loginScreen.signInButton.assertExists()
        true
      } catch (e: AssertionError) {
        false
      }
    }

    // Click on the sign-in button
    loginScreen.signInButton.performClick()

    // Wait for the main screen to be displayed
    val mainScreen = MainPlaceHolder(composeTestRule)
    composeTestRule.waitUntil {
      try {
        mainScreen.singOutButton.assertExists()
        true
      } catch (e: AssertionError) {
        false
      }
    }

    val appBottomBarScreen = AppBottomBarScreen(composeTestRule)
    // Wait for BottomNavigation to be displayed
    composeTestRule.waitUntil {
      try {
        appBottomBarScreen.assertExists()
        true
      } catch (e: AssertionError) {
        false
      }
    }
    appBottomBarScreen.bottomAppBarTrackListButton.performClick()
    appBottomBarScreen.bottomAppBarThemeButton.performClick()
  }
}
