package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen

class LoginScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<LoginScreen>(
        semanticsProvider = semanticsProvider, viewBuilderAction = { hasTestTag("loginScreen") }) {

  val appLogo = onNode { hasTestTag("appIcon") }
  val welcomeTitle = onNode { hasTestTag("welcomeTitle") }
  val welcomeSubtitle = onNode { hasTestTag("welcomeSubtitle") }
  val signInButton = onNode { hasTestTag("signInButton") }
}
