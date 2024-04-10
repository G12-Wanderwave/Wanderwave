package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen

class LogoutScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<LogoutScreen>(
        semanticsProvider = semanticsProvider, viewBuilderAction = { hasTestTag("logoutScreen") }) {

  val logoutProgressIndicator = onNode { hasTestTag("logoutProgressIndicator") }
}
