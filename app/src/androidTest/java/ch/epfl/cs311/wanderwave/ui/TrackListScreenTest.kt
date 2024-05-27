package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationController
import ch.epfl.cs311.wanderwave.model.auth.AuthenticationUserData
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.data.viewModelType
import ch.epfl.cs311.wanderwave.model.localDb.AppDatabase
import ch.epfl.cs311.wanderwave.model.repository.ProfileRepository
import ch.epfl.cs311.wanderwave.model.repository.TrackRepository
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.screens.TrackListScreen
import ch.epfl.cs311.wanderwave.viewmodel.TrackListViewModel
import com.google.common.base.Verify.verify
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class TrackListScreenTest : TestCase() {

  @get:Rule val composeTestRule = createAndroidComposeRule<TestActivity>()

  @get:Rule val mockkRule = MockKRule(this)

  @get:Rule val hiltRule = HiltAndroidRule(this)

  @RelaxedMockK lateinit var mockSpotifyController: SpotifyController
  @RelaxedMockK lateinit var trackRepository: TrackRepository
  @RelaxedMockK lateinit var appDatabase: AppDatabase
  @RelaxedMockK private lateinit var mockNavigationActions: NavigationActions

  @RelaxedMockK lateinit var viewModel: TrackListViewModel

  @RelaxedMockK lateinit var mockShowMessage: (String) -> Unit
  @RelaxedMockK private lateinit var mockNavController: NavHostController

  @Before
  fun setup() {

    every { mockNavController.navigate(any<String>()) } returns Unit
    mockNavigationActions = NavigationActions(mockNavController)
  }

  private fun setupViewModel() {

    flowOf(listOf(Track("id1", "title1", "artist1")))
    every { mockSpotifyController.playTrack(any()) } just Runs
    every { trackRepository.getAll() } returns
        flowOf(
            listOf(
                Track("is 1", "Track 1", "Artist 1"),
                Track("is 2", "Track 2", "Artist 2"),
            ))

    val authenticationController = mockk<AuthenticationController>()

    every { authenticationController.getUserData() } returns
        AuthenticationUserData("id", "email", "name", "image")

    val profile = mockk<Profile>(relaxed = true)
    val profileRepository = mockk<ProfileRepository>(relaxed = true)
    every { profileRepository.getItem(any()) } returns flowOf(Result.success(profile))

    viewModel =
        TrackListViewModel(
            mockSpotifyController,
            appDatabase,
            trackRepository,
            profileRepository,
            authenticationController)
    every { mockSpotifyController.recentlyPlayedTracks.value } returns
        listOf(Track("id1", "title1", "artist1"))
    composeTestRule.setContent { TrackListScreen(mockNavigationActions, viewModel, true) }
  }

  @Test
  fun tappingTrackSelectssIt() = runTest {
    setupViewModel()

    onComposeScreen<TrackListScreen>(composeTestRule) {
      assertIsDisplayed()
      composeTestRule.onNodeWithTag("tab0").performClick()
      composeTestRule.onNodeWithTag("trackListTitle").assertIsDisplayed()
      composeTestRule.onNodeWithTag("trackListTitle").assertTextEquals("Recently Added Tracks")

      composeTestRule.onNodeWithTag("tab1").performClick()
      composeTestRule.onNodeWithTag("trackListTitle").assertIsDisplayed()
      composeTestRule.onNodeWithTag("trackListTitle").assertTextEquals("Liked Tracks")

      composeTestRule.onNodeWithTag("tab2").performClick()
      composeTestRule.onNodeWithTag("trackListTitle").assertIsDisplayed()
      composeTestRule.onNodeWithTag("trackListTitle").assertTextEquals("Banned Tracks")
    }
  }

  @Test
  fun canNavigateToSElectSongScreenFromTrackListScreen() = run {
    mockNavigationActions.navigateToSelectSongScreen(viewModelType.TRACKLIST)
    verify { mockNavController.navigate("${Route.SELECT_SONG.routeString}/tracklist") }
  }
}
