package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen

class TrackListScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<TrackListScreen>(
        semanticsProvider = semanticsProvider,
        viewBuilderAction = { hasTestTag("trackListScreen") }) {

  // val trackButton = onNode { hasTestTag("trackButton") }
  val trackButton = onNode { hasTestTag("trackItem") }
  val tabs = onNode { hasTestTag("tab") }
  val searchBar = onNode { hasTestTag("searchBar") }
  val trackListTitle = onNode { hasTestTag("trackListTitle") }
}
