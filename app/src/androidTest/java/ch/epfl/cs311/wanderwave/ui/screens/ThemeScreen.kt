package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen

class ThemeScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<ThemeScreen>(
        semanticsProvider = semanticsProvider, viewBuilderAction = { hasTestTag("themeScreen") }) {}
