package ch.epfl.cs311.wanderwave.ui.screens.components

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen

class ExclusivePlayerScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<ExclusivePlayerScreen>(
        semanticsProvider = semanticsProvider,
        viewBuilderAction = { hasTestTag("exclusivePlayer") }) {
  val toggleShuffle = onNode { hasTestTag("toggleShuffle") }
  val toggleRepeat = onNode { hasTestTag("toggleRepeat") }
  val playerDragHandle = onNode { hasTestTag("playerDragHandle") }
}
