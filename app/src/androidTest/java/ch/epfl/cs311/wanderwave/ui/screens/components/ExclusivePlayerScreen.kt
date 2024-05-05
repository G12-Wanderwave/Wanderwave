package ch.epfl.cs311.wanderwave.ui.screens.components

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

class ExclusivePlayerScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<ExclusivePlayerScreen>(
        semanticsProvider = semanticsProvider,
        viewBuilderAction = { hasTestTag("exclusivePlayer") }) {
  val switch = onNode { hasTestTag("switch") }
  val broadcastButton = onNode { hasTestTag("broadcastButton") }
  val beaconButton = onNode { hasTestTag("beaconButton") }
  val playlistButton = onNode { hasTestTag("playlistButton") }
  val ignoreButton = onNode { hasTestTag("ignoreButton") }
  val toggleShuffle = onNode { hasTestTag("toggleShuffle") }
  val toggleRepeat = onNode { hasTestTag("toggleRepeat") }

  val playerControl = onNode { hasTestTag("playerControl") }
  val playPauseButton: KNode = playerControl.child { hasTestTag("playPauseButton") }
  val previousButton: KNode = playerControl.child { hasTestTag("previousButton") }
  val nextButton: KNode = playerControl.child { hasTestTag("nextButton") }
  val shuffleButton: KNode = playerControl.child { hasTestTag("shuffleButton") }
  val loopButton: KNode = playerControl.child { hasTestTag("loopButton") }
}
