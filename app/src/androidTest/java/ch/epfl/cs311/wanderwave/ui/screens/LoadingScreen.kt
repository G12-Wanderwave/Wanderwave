package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen

class LoadingScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<LoadingScreen>(
        semanticsProvider = semanticsProvider,
        viewBuilderAction = { hasTestTag("loadingScreen") }) {

  val progressIndicator = onNode { hasTestTag("loadingScreenIndicator") }
}
