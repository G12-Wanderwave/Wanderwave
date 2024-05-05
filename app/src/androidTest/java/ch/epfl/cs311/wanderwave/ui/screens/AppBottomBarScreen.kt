package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import ch.epfl.cs311.wanderwave.navigation.Route
import io.github.kakaocup.compose.node.element.ComposeScreen

class AppBottomBarScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<AppBottomBarScreen>(
        semanticsProvider = semanticsProvider, viewBuilderAction = { hasTestTag("appBottomBar") }) {

  val bottomAppBarTrackListButton = onNode {
    hasTestTag("trackListButton" + Route.TRACK_LIST.routeString)
  }
  val mapScreenButton = onNode { hasTestTag("bottomAppBarButton" + Route.MAP.routeString) }
  val bottomAppBarProfileButton = onNode {
    hasTestTag("bottomAppBarButton" + Route.PROFILE.routeString)
  }
}
