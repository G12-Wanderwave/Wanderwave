package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.ui.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.screens.ProfileScreen
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ProfileTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK
  private lateinit var mockNavigationActions: NavigationActions

  @Before
  fun setup() {

    composeTestRule.setContent { ProfileScreen(mockNavigationActions) }
  }

  @Test
  fun canSeeTheScreen() = run {
    onComposeScreen<ProfileScreen>(composeTestRule) { profileScreen { assertIsDisplayed() } }
  }

  @Test
  fun canSwitchAnonymousMode() = run {
    onComposeScreen<ProfileScreen>(composeTestRule) {
      profileSwitch {
        assertIsDisplayed()
        assertIsOff()
        performClick()
        assertIsOn()
      }
    }
  }

  @Test
  fun editScreen() = run {
    onComposeScreen<ProfileScreen>(composeTestRule) {
      clickableIcon {
        assertIsDisplayed()
        performClick()
      }
      inputFirstName {
        performTextClearance()
        assertIsDisplayed()
        performTextInput("Declan")
        assertTextContains("Declan")
      }
      inputLastName {
        performTextClearance()
        assertIsDisplayed()
        performTextInput("Rice")
        assertTextContains("Rice")
      }
      inputDescription {
        performTextClearance()
        assertIsDisplayed()
        performTextInput("KDOT is back <3")
        assertTextContains("KDOT is back <3")
      }
      cancelButton { assertIsDisplayed() }
      saveButton {
        assertIsDisplayed()
        performClick()
      }
      visitCard { assertIsDisplayed() }
      outputFirstName {
        assertIsDisplayed()
        assertTextContains("Declan")
      }
      outputLastName {
        assertIsDisplayed()
        assertTextContains("Rice")
      }
      outputDescription {
        assertIsDisplayed()
        assertTextContains("KDOT is back <3")
      }
    }
  }
}
