package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import ch.epfl.cs311.wanderwave.ui.navigation.Route
import io.github.kakaocup.compose.node.element.ComposeScreen

class AppScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<AppScreen>(
        semanticsProvider = semanticsProvider, viewBuilderAction = { hasTestTag("app") }) {

  val loginScreenButton = onNode { hasTestTag("bottomAppBarButton" + Route.LOGIN) }
  val trackListScreenButton = onNode { hasTestTag("bottomAppBarButton" + Route.TRACK_LIST) }
  val launchScreenButton = onNode { hasTestTag("bottomAppBarButton" + Route.LAUNCH) }
  val mapScreenButton = onNode { hasTestTag("bottomAppBarButton" + Route.MAP) }

  val loginScreen = onNode { hasTestTag("loginScreen") }
  val trackListScreen = onNode { hasTestTag("trackListScreen") }
  val launchScreen = onNode { hasTestTag("launchScreen") }
  val mapScreen = onNode { hasTestTag("mapScreen") }
}
