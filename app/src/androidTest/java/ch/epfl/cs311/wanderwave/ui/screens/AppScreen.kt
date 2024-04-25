package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

class AppScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<AppScreen>(
        semanticsProvider = semanticsProvider, viewBuilderAction = { hasTestTag("appScreen") }){
    val appBottomBarScreen = onNode { hasTestTag("appBottomBarScreen") }
    val trackListButton: KNode = appBottomBarScreen.child { hasTestTag("trackListButton") }
    }
