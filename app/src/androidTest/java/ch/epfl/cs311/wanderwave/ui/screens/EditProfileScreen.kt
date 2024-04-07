package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen

class EditProfileScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<EditProfileScreen>(
        semanticsProvider = semanticsProvider,
        viewBuilderAction = { hasTestTag("editProfileScreen") }) {

  val inputFirstName = onNode { hasTestTag("firstName") }
  val inputLastName = onNode { hasTestTag("lastName") }
  val inputDescription = onNode { hasTestTag("description") }
  val saveButton = onNode { hasTestTag("saveButton") }
  val cancelButton = onNode { hasTestTag("cancelButton") }
  val deleteButton = onNode { hasTestTag("deleteButton") }
}
