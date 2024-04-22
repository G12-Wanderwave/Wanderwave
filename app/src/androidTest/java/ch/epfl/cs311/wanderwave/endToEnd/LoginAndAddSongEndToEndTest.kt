package ch.epfl.cs311.wanderwave.endToEnd

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepositoryImpl
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.ui.TestActivity
import ch.epfl.cs311.wanderwave.ui.screens.MainPlaceHolder
import ch.epfl.cs311.wanderwave.ui.screens.ProfileScreen
import ch.epfl.cs311.wanderwave.ui.screens.SelectSongScreen
import ch.epfl.cs311.wanderwave.ui.screens.SpotifyConnectScreen
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
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class LoginAndAddSongEndToEndTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createAndroidComposeRule<TestActivity>()

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockNavigationActions: NavigationActions

  @RelaxedMockK private lateinit var mockSpotifyViewModel: SpotifyConnectScreenViewModel

  @RelaxedMockK private lateinit var mockProfileViewModel: ProfileViewModel

  @RelaxedMockK private lateinit var profileRepositoryImpl: ProfileRepositoryImpl

  @RelaxedMockK private lateinit var spotifyController: SpotifyController

  @After
  fun clearMocks() {
    clearAllMocks() // Clear all MockK mocks
  }

  private fun setup(uiState: SpotifyConnectScreenViewModel.UiState) {
    mockDependencies()
    every { mockSpotifyViewModel.uiState } returns MutableStateFlow(uiState)
    mockProfileViewModel = ProfileViewModel(profileRepositoryImpl, spotifyController)

    composeTestRule.setContent {
      SpotifyConnectScreen(navigationActions = mockNavigationActions, viewModel = mockSpotifyViewModel)
      MainPlaceHolder(mockNavigationActions)
      ProfileScreen(mockNavigationActions, mockProfileViewModel)
      SelectSongScreen(mockNavigationActions, mockProfileViewModel)
    }
  }

  private fun mockDependencies() {
    // Mocking ProfileRepositoryImpl
    coEvery { profileRepositoryImpl.insert(any()) } just Runs
    coEvery { profileRepositoryImpl.delete() } just Runs

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
  fun loginNavigateToProfileAndAddSong_SuccessfulFlow() =
      runBlockingTest() {
        setup(SpotifyConnectScreenViewModel.UiState(hasResult = true, success = true))

        ComposeScreen.onComposeScreen<MainPlaceHolder>(composeTestRule) {
          assertIsDisplayed()
          profileButton.assertIsDisplayed()
          profileButton.performClick()
        }

        ComposeScreen.onComposeScreen<ProfileScreen>(composeTestRule) {
          assertIsDisplayed()
          addTopSongs.assertIsDisplayed()
          addTopSongs.performClick()
        }
        val expectedListItem = ListItem("id", "title", null, "subtitle", "", false, true)

        every { spotifyController.getAllElementFromSpotify() } returns
            flowOf(listOf(expectedListItem))
        every {
          spotifyController.getAllChildren(
              ListItem("id", "title", null, "subtitle", "", false, true))
        } returns flowOf(listOf(expectedListItem))

        mockProfileViewModel.retrieveAndAddSubsection(this)
        mockProfileViewModel.retrieveChild(expectedListItem, this)
        advanceUntilIdle() // Ensure all coroutines are completed

        val flow = mockProfileViewModel.spotifySubsectionList
        val flow2 = mockProfileViewModel.childrenPlaylistTrackList
        val result = flow.timeout(2.seconds).catch {}.firstOrNull()
        val result2 = flow2.timeout(2.seconds).catch {}.firstOrNull()

        assertEquals(expectedListItem, result?.get(0))
        assertEquals(expectedListItem, result2?.get(0))

        ComposeScreen.onComposeScreen<SelectSongScreen>(composeTestRule) {
          assertIsDisplayed()
          trackItemCard.assertIsDisplayed()
          trackItemCard.performClick()
        }

        ComposeScreen.onComposeScreen<ProfileScreen>(composeTestRule) {
          assertIsDisplayed()
          addTopSongs.assertIsDisplayed()
          trackItemCard.assertIsDisplayed()
        }
      }
}
