package ch.epfl.cs311.wanderwave.ui.screens.components

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen

class MiniPlayerScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<MiniPlayerScreen>(
        semanticsProvider = semanticsProvider, viewBuilderAction = { hasTestTag("miniPlayer") }) {
  val miniPlayerTitleButton = onNode { hasTestTag("miniPlayerTitleButton") }
  val playPauseButton = onNode { hasTestTag("playPauseButton") }
}
