package ch.epfl.cs311.wanderwave.ui.screens.components

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen

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
}
