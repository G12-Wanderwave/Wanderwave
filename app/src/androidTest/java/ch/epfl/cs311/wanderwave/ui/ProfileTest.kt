package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.viewModelScope
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.remote.ProfileConnection
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.screens.ProfileScreen
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.spotify.protocol.types.ListItem
import dagger.hilt.android.testing.HiltAndroidRule
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
class ProfileTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createAndroidComposeRule<TestActivity>()

  @get:Rule val mockkRule = MockKRule(this)

  @get:Rule val hiltRule = HiltAndroidRule(this)

  @RelaxedMockK private lateinit var mockNavigationActions: NavigationActions
  lateinit var viewModel: ProfileViewModel
  val testDispatcher = CoroutineScope(Dispatchers.Unconfined)
  @RelaxedMockK private lateinit var profileRepository: ProfileConnection

  @RelaxedMockK private lateinit var spotifyController: SpotifyController

  @RelaxedMockK private lateinit var authenticationController: AuthenticationController

  @Before
  fun setup() {
    mockDependencies()
    viewModel = ProfileViewModel(profileRepository, spotifyController, authenticationController)

    composeTestRule.setContent { ProfileScreen(mockNavigationActions, viewModel, true) }
  }

  @After
  fun tearDown() {
    try {
      viewModel.viewModelScope.cancel()
    } finally {
      Dispatchers.resetMain() // Reset the main dispatcher to the original one
    }
  }

  @Test
  fun profileScreeIsDisplay() = run {
    onComposeScreen<ProfileScreen>(composeTestRule) { assertIsDisplayed() }
  }

  private fun mockDependencies() {
    // Mocking ProfileRepositoryImpl
    coEvery { profileRepository.addItem(any()) } just Runs
    coEvery { profileRepository.deleteItem(any<String>()) } just Runs
    coEvery { profileRepository.deleteItem(any<Profile>()) } just Runs

    // Mocking SpotifyController
    coEvery { spotifyController.getChildren(any()) } returns
        flowOf(ListItem("", "", null, "", "", false, false))
    coEvery { spotifyController.getAllElementFromSpotify() } returns
        flowOf(listOf(ListItem("", "", null, "", "", false, false)))
    coEvery { spotifyController.getAllChildren(any()) } returns
        flowOf(listOf(ListItem("", "", null, "", "", false, false)))
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

  @Test
  fun canNavigateToAbout() = run {
    onComposeScreen<ProfileScreen>(composeTestRule) {
      aboutButton.assertIsDisplayed()
      aboutButton.performClick()
      verify { mockNavigationActions.navigateTo(Route.ABOUT) }
    }
  }
}
