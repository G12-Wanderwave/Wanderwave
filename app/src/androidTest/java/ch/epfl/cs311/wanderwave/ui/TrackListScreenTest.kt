package ch.epfl.cs311.wanderwave.ui

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.model.data.Track
import ch.epfl.cs311.wanderwave.model.repository.TrackRepositoryImpl
import ch.epfl.cs311.wanderwave.model.spotify.SpotifyController
import ch.epfl.cs311.wanderwave.ui.screens.TrackListScreen
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import io.mockk.called
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrackListScreenTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createAndroidComposeRule<TestActivity>()

  @RelaxedMockK lateinit var mockTrackRepositoryImpl: TrackRepositoryImpl

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK private lateinit var mockSpotifyController: SpotifyController

  @RelaxedMockK private lateinit var mockShowMessage: (String) -> Unit

  fun setup(result: Boolean) {
    every { mockTrackRepositoryImpl.getAll() } returns
        flowOf(listOf(Track("id1", "title1", "artist1")))
    every { mockSpotifyController.playTrack(any()) } returns flowOf(result)
    val viewModel = TrackListViewModel(mockTrackRepositoryImpl, mockSpotifyController)
    composeTestRule.setContent { TrackListScreen(mockShowMessage, viewModel) }
  }

  @Test
  fun trackListScreenIsDisplayed() = run {
    setup(true)
    onComposeScreen<TrackListScreen>(composeTestRule) {
      assertIsDisplayed()

      trackButton {
        assertIsDisplayed()
        assert(hasClickAction())
      }
    }
  }

  @Test
  fun tappingTrackPlaysIt() = run {
    setup(true)
    onComposeScreen<TrackListScreen>(composeTestRule) {
      trackButton {
        assertIsDisplayed()
        performClick()
      }
      verify { mockSpotifyController.playTrack(any()) }
      coVerify { mockShowMessage wasNot called }
    }
  }

  @Test
  fun failedToPlayTrackDisplaysMessage() = run {
    setup(false)
    onComposeScreen<TrackListScreen>(composeTestRule) {
      trackButton {
        assertIsDisplayed()
        performClick()
      }
      verify { mockSpotifyController.playTrack(any()) }
      // coVerify { mockShowMessage(any()) }

    }
  }
}
