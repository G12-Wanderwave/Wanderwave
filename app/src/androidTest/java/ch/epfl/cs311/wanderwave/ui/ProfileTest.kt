package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.ui.screens.ProfileScreen
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel
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

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setup() {

    val vm = ProfileViewModel()
    composeTestRule.setContent { ProfileScreen(vm) }
  }

  @Test
  fun canToggleSongLists() = run {
    onComposeScreen<ProfileScreen>(composeTestRule) {
      // Assume we have a button with testTag "toggleSongList"
      composeTestRule.onNodeWithTag("toggleSongList").assertIsDisplayed().performClick()

      // Now, the text should change to indicate the list has toggled
      // If "TOP SONGS" is visible, it should show "Show CHOSEN SONGS"
      composeTestRule.onNodeWithText("Show CHOSEN SONGS").assertExists()

      // Perform another click to toggle back
      composeTestRule.onNodeWithTag("toggleSongList").performClick()

      // Verify if "TOP SONGS" list is shown again
      composeTestRule.onNodeWithText("Show TOP SONGS").assertExists()
    }
  }

  @Test
  fun addTrackToTopSongsList() = run {
    onComposeScreen<ProfileScreen>(composeTestRule) {
      // Open the dialog to add a track to "TOP SONGS"
      // Assuming the button has testTag "addTopSongs"
      composeTestRule.onNodeWithTag("addTopSongs").performClick()

      // Fill out the dialog fields and add the track
      // Assuming dialog fields have testTags "trackIdInput", "trackTitleInput", "trackArtistInput"
      composeTestRule.onNodeWithTag("trackIdInput").performTextInput("1")
      composeTestRule.onNodeWithTag("trackTitleInput").performTextInput("New Top Song")
      composeTestRule.onNodeWithTag("trackArtistInput").performTextInput("Top Artist")

      // Assuming the "Add" button in the dialog has testTag "confirmAddTrack"
      composeTestRule.onNodeWithTag("confirmAddTrack").performClick()

      // Wait for the UI to become idle
      composeTestRule.waitForIdle()

      // Check if the track was added by looking for its title
      // Assuming each TrackItem has its title as the text on the screen
      composeTestRule.onNodeWithText("New Top Song").assertExists()
    }
  }

  @Test
  fun addTrackToChosenSongsList() = run {
    onComposeScreen<ProfileScreen>(composeTestRule) {
      // Open the dialog to add a track to "CHOSEN SONGS"
      // Assuming the button has testTag "addChosenSongs"
      composeTestRule.onNodeWithTag("addChosenSongs").performClick()

      // Fill out the dialog fields and add the track
      composeTestRule.onNodeWithTag("trackIdInput").performTextInput("2")
      composeTestRule.onNodeWithTag("trackTitleInput").performTextInput("New Chosen Song")
      composeTestRule.onNodeWithTag("trackArtistInput").performTextInput("Chosen Artist")

      // Confirm the addition of the track
      composeTestRule.onNodeWithTag("confirmAddTrack").performClick()

      // Toggle to the "CHOSEN SONGS" list to see the newly added track
      composeTestRule.onNodeWithTag("toggleSongList").performClick()

      // Check if the track was added
      composeTestRule.onNodeWithText("New Chosen Song").assertExists()
    }
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
