package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen

class LaunchScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<LaunchScreen>(
        semanticsProvider = semanticsProvider, viewBuilderAction = { hasTestTag("launchScreen") })
