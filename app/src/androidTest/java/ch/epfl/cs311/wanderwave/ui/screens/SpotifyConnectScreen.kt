package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen

class SpotifyConnectScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<SpotifyConnectScreen>(
        semanticsProvider = semanticsProvider,
        viewBuilderAction = { hasTestTag("spotifyConnectScreen") }) {

  val spotifyConnectProgressIndicator = onNode { hasTestTag("spotifyConnectProgressIndicator") }
}
