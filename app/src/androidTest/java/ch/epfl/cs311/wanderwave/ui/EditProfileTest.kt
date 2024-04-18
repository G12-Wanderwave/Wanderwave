package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.viewModelScope
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepositoryImpl
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.screens.EditProfileScreen
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.spotify.protocol.types.ListItem
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class EditProfileTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createAndroidComposeRule<TestActivity>()

  @get:Rule val mockkRule = MockKRule(this)

  lateinit var viewModel: ProfileViewModel

  @RelaxedMockK private lateinit var mockNavigationActions: NavigationActions
  val testDispatcher = CoroutineScope(Dispatchers.Unconfined)
  @RelaxedMockK private lateinit var profileRepositoryImpl: ProfileRepositoryImpl

  @RelaxedMockK private lateinit var spotifyController: SpotifyController

  @Before
  fun setup() {
    mockDependencies()
    viewModel = ProfileViewModel(profileRepositoryImpl, spotifyController)

    composeTestRule.setContent { EditProfileScreen(mockNavigationActions,viewModel) }
  }
  @After
  fun tearDown() {
    try {
      // Cancel any ongoing coroutines started by the ViewModel
      viewModel.viewModelScope.cancel()
      // Cleanup test coroutines to avoid leaking them
//      testDispatcher.cleanupTestCoroutines()
    } finally {
      Dispatchers.resetMain() // Reset the main dispatcher to the original one
    }
  }
  @Test
  fun profileScreenIsDisplayed() = run {
    onComposeScreen<EditProfileScreen>(composeTestRule) { assertIsDisplayed() }
  }
  private fun mockDependencies() {
    // Mocking ProfileRepositoryImpl
    coEvery { profileRepositoryImpl.insert(any()) } just Runs
    coEvery { profileRepositoryImpl.delete() } just Runs

    // Mocking SpotifyController
    coEvery { spotifyController.getTrack() } returns flowOf(ListItem("", "", null, "", "", false, false))
    coEvery { spotifyController.getChildren(any()) } returns flowOf(ListItem("", "", null, "", "", false, false))
    coEvery { spotifyController.getAllElementFromSpotify() } returns flowOf(listOf(ListItem("", "", null, "", "", false, false)))
    coEvery { spotifyController.getAllChildren(any()) } returns flowOf(listOf(ListItem("", "", null, "", "", false, false)))
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
        verify { mockNavigationActions.navigateToTopLevel(Route.PROFILE) }
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
