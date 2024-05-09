package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen

class BeaconScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<BeaconScreen>(
        semanticsProvider = semanticsProvider, viewBuilderAction = { hasTestTag("beaconScreen") }) {

  val beaconTitle = onNode { hasTestTag("beaconTitle") }
  val beaconLocation = onNode { hasTestTag("beaconLocation") }
  val beaconMap = onNode { hasTestTag("beaconMap") }
  val beaconTracksTitle = onNode { hasTestTag("trackListTitle") }
  val trackItem = onNode { hasTestTag("trackItem") }
}
