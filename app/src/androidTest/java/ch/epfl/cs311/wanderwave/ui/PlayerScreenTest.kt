package ch.epfl.cs311.wanderwave.ui

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.ui.components.player.ExclusivePlayer
import ch.epfl.cs311.wanderwave.ui.components.player.MiniPlayer
import ch.epfl.cs311.wanderwave.ui.screens.components.ExclusivePlayerScreen
import ch.epfl.cs311.wanderwave.viewmodel.PlayerViewModel
import ch.epfl.cs311.wanderwave.ui.screens.components.MiniPlayerScreen
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class PlayerScreenTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createAndroidComposeRule<TestActivity>()

  private lateinit var viewModel: PlayerViewModel
  @get:Rule val hiltRule = HiltAndroidRule(this)

  @Before
  fun setup() {
    viewModel = mockk<PlayerViewModel>(relaxed = true)
    every { viewModel.uiState } returns MutableStateFlow(PlayerViewModel.UiState())
  }

  @Test
  fun exclusivePlayerScreenInteractionsAndComponentsDisplayed() = run {
    composeTestRule.setContent {
      val checked = remember { mutableStateOf(false) }
      val selectedVote = remember { mutableIntStateOf(0) }
      val progress = remember { mutableFloatStateOf(0f) }
      ExclusivePlayer(
          checked = checked,
          selectedVote = selectedVote,
          uiState = viewModel.uiState.collectAsState().value,
          progress = progress)
    }

    onComposeScreen<ExclusivePlayerScreen>(composeTestRule) {
      assertIsDisplayed()
      playerDragHandle.assertIsDisplayed()
      votingButtons.assertIsDisplayed()
      toggleShuffle.performClick()
      toggleShuffle.performClick()
      toggleRepeat.performClick()
      toggleRepeat.performClick()
      toggleRepeat.performClick()
      switch.performClick()
      broadcastButton.performClick()
      beaconButton.performClick()
      playlistButton.performClick()
      ignoreButton.performClick()
    }
  }

  fun miniPlayerScreenInteractionsAndComponentsDisplayed() = run {
    composeTestRule.setContent {
      val progress = remember { mutableFloatStateOf(0f) }
      MiniPlayer(
          uiState = viewModel.uiState.collectAsState().value,
          onTitleClick = { viewModel.expand() },
          onPlayClick = { viewModel.resume() },
          onPauseClick = { viewModel.pause() },
          progress = progress)
    }

    onComposeScreen<MiniPlayerScreen>(composeTestRule) {
      assertIsDisplayed()
      miniPlayerTitleButton.assertIsDisplayed()
      playPauseButton.performClick()
      playPauseButton.performClick()
      miniPlayerTitleButton.performClick()
    }
    onComposeScreen<ExclusivePlayerScreen>(composeTestRule) { assertIsDisplayed() }
  }
}
