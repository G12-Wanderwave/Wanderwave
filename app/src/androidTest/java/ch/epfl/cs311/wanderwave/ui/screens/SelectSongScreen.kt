package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen

class SelectSongScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<SelectSongScreen>(
        semanticsProvider = semanticsProvider,
        viewBuilderAction = { hasTestTag("selectSongScreen") }) {


    val trackItemCard = onNode { hasTestTag("trackItemCard") }
}
