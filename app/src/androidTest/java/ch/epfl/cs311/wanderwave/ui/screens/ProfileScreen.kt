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
  val inputFirstName = onNode { hasTestTag("firstName") }
  val inputLastName = onNode { hasTestTag("lastName") }
  val inputDescription = onNode { hasTestTag("description") }
  val saveButton = onNode { hasTestTag("saveButton") }
  val cancelButton = onNode { hasTestTag("cancelButton") }

  val outputFirstName = onNode { hasTestTag("outputFirstName") }
  val outputLastName = onNode { hasTestTag("outputLastName") }
  val outputDescription = onNode { hasTestTag("outputDescription") }
}
