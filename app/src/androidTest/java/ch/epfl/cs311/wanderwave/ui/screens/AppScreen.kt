package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen

class AppScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<AppScreen>(
        semanticsProvider = semanticsProvider, viewBuilderAction = { hasTestTag("appScreen") })
