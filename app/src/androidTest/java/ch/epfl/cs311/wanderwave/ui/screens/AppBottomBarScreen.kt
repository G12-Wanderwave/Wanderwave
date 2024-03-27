package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import ch.epfl.cs311.wanderwave.ui.navigation.Route
import io.github.kakaocup.compose.node.element.ComposeScreen

class AppBottomBarScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<AppBottomBarScreen>(
        semanticsProvider = semanticsProvider, viewBuilderAction = { hasTestTag("appBottomBar") }) {

  val bottomAppBarLaunchButton = onNode {
    hasTestTag("bottomAppBarButton" + Route.LAUNCH.routeString)
  }
  val bottomAppBarLoginButton = onNode {
    hasTestTag("bottomAppBarButton" + Route.LOGIN.routeString)
  }
  val bottomAppBarTrackListButton = onNode {
    hasTestTag("bottomAppBarButton" + Route.TRACK_LIST.routeString)
  }
  val bottomAppBarMainPlaceHolderButton = onNode {
    hasTestTag("bottomAppBarButton" + Route.MAIN.routeString)
  }
}
