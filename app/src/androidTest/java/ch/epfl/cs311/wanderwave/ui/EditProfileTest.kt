package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.ui.screens.EditProfileScreen
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class EditProfileTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createAndroidComposeRule<TestActivity>()

  @Before
  fun setup() {
    var profile =
        Profile(
            firstName = "My FirstName",
            lastName = "My LastName",
            description = "My Description",
            numberOfLikes = 0,
            isPublic = true,
            profilePictureUri = null,
            firebaseUid = "My Firebase UID",
            spotifyUid = "My Spotify UID")
    composeTestRule.setContent {
      EditProfileScreen(
          profile = profile, onProfileChange = { updatedProfile -> profile = updatedProfile })
    }
  }

  @Test
  fun profileScreeIsDisplay() = run {
    ComposeScreen.onComposeScreen<EditProfileScreen>(composeTestRule) { assertIsDisplayed() }
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
      }
    }
  }

  @Test
  fun deleteProfile() = run {
    onComposeScreen<EditProfileScreen>(composeTestRule) {
      deleteButton {
        assertIsDisplayed()
        performClick()
      }
    }
  }
}
