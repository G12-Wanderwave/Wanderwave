package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.navigation.NavigationActions
import ch.epfl.cs311.wanderwave.navigation.Route
import ch.epfl.cs311.wanderwave.ui.components.profile.ImageSelection
import ch.epfl.cs311.wanderwave.ui.theme.md_theme_light_error
import ch.epfl.cs311.wanderwave.ui.theme.md_theme_light_primary
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel

/**
 * Display the profile of the user, with the possibility to edit it
 *
 * @param profile the profile of the user
 * @param onProfileChange enable to transmit the changed to the caller
 * @param viewModel the viewModel that will handle the profile
 * @author Menzo Bouaissi
 * @since 1.0
 * @last update 1.0
 */
@Composable
fun EditProfileScreen(navActions: NavigationActions, viewModel: ProfileViewModel) {
  val profile by viewModel.profile.collectAsState()
  val profile2 = profile.copy()
  var firstName by remember { mutableStateOf(profile2.firstName) }
  var lastName by remember { mutableStateOf(profile2.lastName) }
  var description by remember { mutableStateOf(profile2.description) }

  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.testTag("editProfileScreen")) {
        ImageSelection(
            profile = profile2, onImageChange = { uri -> profile2.profilePictureUri = uri })
        EditableTextFields(
            firstName = firstName,
            lastName = lastName,
            description = description,
            onFirstNameChange = { newName ->
              if (newName.length <= MAX_NBR_CHAR_NAMES) firstName = newName
            },
            onLastNameChange = { newName ->
              if (newName.length <= MAX_NBR_CHAR_NAMES) lastName = newName
            },
            onDescriptionChange = { newDescription ->
              if (newDescription.length <= MAX_NBR_CHAR_DESC) description = newDescription
            })
        Spacer(Modifier.padding(18.dp))
        ActionButtons(
            onSave = {
              val profileCopy =
                  profile.copy(
                      firstName = firstName,
                      lastName = lastName,
                      description = description,
                      profilePictureUri = profile2.profilePictureUri,
                  )

              viewModel.updateProfile(profileCopy)
              navActions.goBack()
            },
            onCancel = { navActions.goBack() },
            onDelete = {
              viewModel.deleteProfile()
              navActions.navigateToTopLevel(Route.LOGIN)
            })
      }
}

/**
 * TextFields that can be edited, and modify the profile of the user
 *
 * @param firstName the first name of the user
 * @param lastName the last name of the user
 * @param description the description of the user
 * @param onFirstNameChange enable to transmit the changed to the caller
 * @param onLastNameChange enable to transmit the changed to the caller
 * @param onDescriptionChange enable to transmit the changed to the caller
 * @author Menzo Bouaissi
 * @since 1.0
 * @last update 1.0
 */
@Composable
fun EditableTextFields(
    firstName: String,
    lastName: String,
    description: String,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit
) {
  Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
          OutlinedTextField(
              value = firstName,
              modifier =
                  Modifier.height(IntrinsicSize.Min)
                      .padding(horizontal = 8.dp)
                      .width(INPUT_BOX_NAM_SIZE)
                      .testTag("firstName"),
              onValueChange = onFirstNameChange,
              label = { Text("First Name") })
          OutlinedTextField(
              value = lastName,
              modifier =
                  Modifier.height(IntrinsicSize.Min)
                      .padding(horizontal = 8.dp)
                      .width(INPUT_BOX_NAM_SIZE)
                      .testTag("lastName"),
              onValueChange = onLastNameChange,
              label = { Text("Last Name") })
        }
        OutlinedTextField(
            value = description,
            modifier =
                Modifier.height(IntrinsicSize.Min)
                    .width(338.dp)
                    .padding(horizontal = 8.dp)
                    .testTag("description"),
            onValueChange = onDescriptionChange,
            label = { Text("Description") })
      }
}

/**
 * Actions button to save or cancel the changes
 *
 * @param onSave the action to be done when the user wants to save the changes
 * @param onCancel the action to be done when the user wants to cancel the changes
 * @author Menzo Bouaissi
 * @since 1.0
 * @last update 1.0
 */
@Composable
fun ActionButtons(onSave: () -> Unit, onCancel: () -> Unit, onDelete: () -> Unit) {
  Column(
      verticalArrangement = Arrangement.spacedBy(2.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = onSave,
            colors = ButtonDefaults.buttonColors(containerColor = md_theme_light_primary),
            modifier = Modifier.width(100.dp).testTag("saveButton")) {
              Text("Save")
            }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, md_theme_light_error),
            modifier = Modifier.width(100.dp).testTag("cancelButton")) {
              Text(text = "Cancel", color = md_theme_light_error)
            }
        Spacer(Modifier.padding(8.dp))
        Button(
            onClick = onDelete,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, md_theme_light_error),
            modifier = Modifier.width(200.dp).testTag("deleteButton")) {
              Text(text = "Delete profile", color = md_theme_light_error)
            }
      }
}
