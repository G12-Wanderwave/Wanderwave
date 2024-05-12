package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

class AppScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<AppScreen>(
        semanticsProvider = semanticsProvider, viewBuilderAction = { hasTestTag("appScreen") }) {
  val appScaffold = onNode { hasTestTag("appScaffold") }
  val appBottomBar: KNode = appScaffold.child { hasTestTag("appBottomBar") }
  val trackListButton: KNode = appBottomBar.child { hasTestTag("bottomAppBarButtontrackList") }

  val miniPlayer: KNode = appScaffold.child { hasTestTag("miniPlayer") }
  val miniPlayerTitle: KNode = miniPlayer.child { hasTestTag("miniPlayerTitle") }
  val miniPlayerPlayButton: KNode = miniPlayer.child { hasTestTag("miniPlayerPlayButton") }
}
