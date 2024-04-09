package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen

class AppBottomBarScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<AppBottomBarScreen>(
        semanticsProvider = semanticsProvider, viewBuilderAction = { hasTestTag("appBottomBar") }) {

  val bottomAppBarTrackListButton = onNode { hasTestTag("bottomAppBarButton") }
  val bottomAppBarMainPlaceHolderButton = onNode { hasTestTag("bottomAppBarButton") }
  val mapScreenButton = onNode { hasTestTag("bottomAppBarButton") }
}
