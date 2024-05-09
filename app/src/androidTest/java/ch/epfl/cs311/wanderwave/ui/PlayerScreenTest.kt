import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.cs311.wanderwave.ui.TestActivity
import ch.epfl.cs311.wanderwave.ui.components.player.ExclusivePlayer
import ch.epfl.cs311.wanderwave.ui.screens.components.ExclusivePlayerScreen
import ch.epfl.cs311.wanderwave.viewmodel.PlayerViewModel
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerScreenTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createAndroidComposeRule<TestActivity>()

  private lateinit var viewModel: PlayerViewModel

  @Before
  fun setup() {
    viewModel = mockk<PlayerViewModel>(relaxed = true)
    every { viewModel.uiState } returns MutableStateFlow(PlayerViewModel.UiState())
  }

  @Test
  fun exclusivePlayerScreenInteractions() = run {
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
}
