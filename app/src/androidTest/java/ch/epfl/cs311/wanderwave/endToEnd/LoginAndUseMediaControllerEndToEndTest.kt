package ch.epfl.cs311.wanderwave.endToEnd

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.TestActivity
import ch.epfl.cs311.wanderwave.ui.screens.AppScreen
import ch.epfl.cs311.wanderwave.ui.screens.SpotifyConnectScreen
import ch.epfl.cs311.wanderwave.ui.screens.TrackListScreen
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel
import ch.epfl.cs311.wanderwave.viewmodel.SpotifyConnectScreenViewModel
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.spotify.protocol.types.ListItem
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class LoginAndUseMediaControllerEndToEndTest :
    TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createAndroidComposeRule<TestActivity>()

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockNavigationActions: NavigationActions

  @RelaxedMockK private lateinit var mockSpotifyViewModel: SpotifyConnectScreenViewModel

  @RelaxedMockK private lateinit var mockProfileViewModel: ProfileViewModel

  @RelaxedMockK private lateinit var profileRepositoryImpl: ProfileRepository

  @RelaxedMockK private lateinit var spotifyController: SpotifyController

  @After
  fun clearMocks() {
    clearAllMocks() // Clear all MockK mocks
  }

  private fun setup(uiState: SpotifyConnectScreenViewModel.UiState) {
    mockDependencies()
    every { mockSpotifyViewModel.uiState } returns MutableStateFlow(uiState)
    mockProfileViewModel = ProfileViewModel(profileRepositoryImpl, spotifyController)

    // Mock navigation actions to track list
    every { mockNavigationActions.navigateTo(Route.TRACK_LIST) } just Runs

    composeTestRule.setContent {
      SpotifyConnectScreen(
          navigationActions = mockNavigationActions, viewModel = mockSpotifyViewModel)
      AppScreen(composeTestRule)
      TrackListScreen(showMessage = { _ -> })
    }
  }

  private fun mockDependencies() {
    // Mocking ProfileRepositoryImpl
    coEvery { profileRepositoryImpl.addItem(any()) } just Runs
    coEvery { profileRepositoryImpl.deleteItem(any<Profile>()) } just Runs
    coEvery { profileRepositoryImpl.deleteItem(any<String>()) } just Runs

    // Mocking SpotifyController
    coEvery { spotifyController.getChildren(any()) } returns
        flowOf(ListItem("", "", null, "", "", false, false))
    coEvery { spotifyController.getAllElementFromSpotify() } returns
        flowOf(listOf(ListItem("", "", null, "", "", false, false)))
    coEvery { spotifyController.getAllChildren(any()) } returns
        flowOf(listOf(ListItem("", "", null, "", "", false, false)))
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun testEndToEnd() = runBlockingTest {
    setup(SpotifyConnectScreenViewModel.UiState(hasResult = true, success = true))

    ComposeScreen.onComposeScreen<AppScreen>(composeTestRule) {
      assertIsDisplayed()
      appScaffold.assertIsDisplayed()
    }

    // TODO: Find a way to test the bottom bar
    mockNavigationActions.navigateTo(Route.TRACK_LIST)

    ComposeScreen.onComposeScreen<TrackListScreen>(composeTestRule) {
      assertIsDisplayed()
      trackButton.assertIsDisplayed()
      searchBar.assertIsDisplayed()

      trackButton.performClick()
    }
  }
}
