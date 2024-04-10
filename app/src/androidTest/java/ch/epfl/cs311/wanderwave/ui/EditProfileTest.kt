package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.screens.EditProfileScreen
import ch.epfl.cs311.wanderwave.ui.screens.ProfileScreen
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class EditProfileTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createAndroidComposeRule<TestActivity>()

  @RelaxedMockK private lateinit var mockNavigationActions: NavigationActions


  @Before
  fun setup() {
    composeTestRule.setContent {
      EditProfileScreen(mockNavigationActions)
    }
  }

  @Test
  fun profileScreenIsDisplayed() = run {
    onComposeScreen<EditProfileScreen>(composeTestRule) { assertIsDisplayed() }
  }

  @Test
  fun editScreen() = run {
    onComposeScreen<EditProfileScreen>(composeTestRule) {
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
        onComposeScreen<ProfileScreen>(composeTestRule) { assertIsDisplayed() }
      }
    }
  }

  @Test
  fun cancelEdit() = run {
    onComposeScreen<EditProfileScreen>(composeTestRule) {
      cancelButton {
        assertIsDisplayed()
        performClick()
        verify { mockNavigationActions.navigateToTopLevel(Route.PROFILE) }
      }
    }
  }

  @Test
  fun deleteProfile() = run {
    onComposeScreen<EditProfileScreen>(composeTestRule) {
      deleteButton {
        assertIsDisplayed()
        performClick()
        verify { mockNavigationActions.navigateToTopLevel(Route.LOGIN) }
      }
    }
  }


}
