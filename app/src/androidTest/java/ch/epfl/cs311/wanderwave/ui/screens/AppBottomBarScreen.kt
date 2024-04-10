package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen

class AppBottomBarScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<AppBottomBarScreen>(
        semanticsProvider = semanticsProvider, viewBuilderAction = { hasTestTag("appBottomBar") }) {

  val bottomAppBarTrackListButton = onNode { hasTestTag("bottomAppBarTrackListButton") }
  val bottomAppBarMainPlaceHolderButton = onNode { hasTestTag("bottomAppBarMainPlaceHolderButton") }
  val mapScreenButton = onNode { hasTestTag("bottomAppBarMapButton") }
}
