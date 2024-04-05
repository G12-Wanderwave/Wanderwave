package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.ui.screens.ProfileScreen
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ProfileTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createAndroidComposeRule<TestActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent { ProfileScreen() }
  }

  @Test
  fun profileScreeIsDisplay() = run {
    onComposeScreen<ProfileScreen>(composeTestRule) { assertIsDisplayed() }
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
  fun everythingIsDisplayed() = run {
    onComposeScreen<ProfileScreen>(composeTestRule) {
      visitCard.assertIsDisplayed()
      profileScreen.assertIsDisplayed()
      profileSwitch.assertIsDisplayed()
      clickableIcon.assertIsDisplayed()
      outputFirstName {
        assertIsDisplayed()
        assertTextContains("My FirstName")
      }
      outputDescription {
        assertIsDisplayed()
        assertTextContains("My Description")
      }
      outputLastName {
        assertIsDisplayed()
        assertTextContains("My LastName")
      }
    }
  }
}
