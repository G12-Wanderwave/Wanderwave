package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen

class ProfileScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<ProfileScreen>(
        semanticsProvider = semanticsProvider,
        viewBuilderAction = { hasTestTag("profileScreen") }) {

  val profileScreen = onNode { hasTestTag("profileScreen") }
  val visitCard = onNode { hasTestTag("visitCard") }
  val profileSwitch = onNode { hasTestTag("profileSwitch") }
  val clickableIcon = onNode { hasTestTag("clickableIcon") }

  val outputFirstName = onNode { hasTestTag("outputFirstName") }
  val outputLastName = onNode { hasTestTag("outputLastName") }
  val outputDescription = onNode { hasTestTag("outputDescription") }

  val addTopSongs = onNode { hasTestTag("addTopSongs") }
  val trackItemCard = onNode { hasTestTag("trackItemCard") }

  val aboutButton = onNode { hasTestTag("aboutButton") }
  val signOutButton = onNode { hasTestTag("signOutButton") }
  val addTrackButton = onNode { hasTestTag("addTrackButton") }
  val trackCard = onNode { hasTestTag("trackCard") }
}
