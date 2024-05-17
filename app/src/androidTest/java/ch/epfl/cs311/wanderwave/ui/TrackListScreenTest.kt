package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.data.viewModelType
import ch.epfl.cs311.wanderwave.model.localDb.AppDatabase
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
import io.mockk.Called
import io.mockk.Runs
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
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

  private fun setupViewModel(result: Boolean) {

    flowOf(listOf(Track("id1", "title1", "artist1")))
    every { mockSpotifyController.playTrack(any()) } just Runs
    every { trackRepository.getAll() } returns
        flowOf(
            listOf(
                Track("is 1", "Track 1", "Artist 1"),
                Track("is 2", "Track 2", "Artist 2"),
            ))

    viewModel = TrackListViewModel(mockSpotifyController, appDatabase, trackRepository)

    composeTestRule.setContent {
      TrackListScreen(mockNavigationActions, mockShowMessage, viewModel)
    }
  }

  @Test
  fun tappingTrackSelectssIt() = runTest {
    setupViewModel(true)

    onComposeScreen<TrackListScreen>(composeTestRule) {
      trackButton {
        assertIsDisplayed()
        performClick()
      }
      advanceUntilIdle()
      coVerify { mockShowMessage wasNot Called }
    }
  }

  @Test
  fun canNavigateToSElectSongScreenFromTrackListScreen() = run {
    mockNavigationActions.navigateToSelectSongScreen(viewModelType.TRACKLIST)
    verify { mockNavController.navigate("${Route.SELECT_SONG.routeString}/tracklist") }
  }
}
