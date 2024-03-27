package ch.epfl.cs311.wanderwave.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.ui.theme.md_theme_light_error
import ch.epfl.cs311.wanderwave.ui.theme.md_theme_light_primary
import ch.epfl.cs311.wanderwave.viewmodel.ProfileViewModel
import coil.compose.AsyncImage

const val SCALE_X = 0.5f
const val SCALE_Y = 0.5f
const val MAX_NBR_CHAR_NAMES = 12
const val MAX_NBR_CHAR_DESC = 35
val INPUT_BOX_NAM_SIZE = 150.dp

/**
 * This is the screen composable, which can either show the profile of the user and the chosen
 * songs, or it could show a view to modify the profile
 *
 * @param viewModel the viewModel that will handle the profile *
 * @author Menzo Bouaissi
 * @since 1.0
 * @last update 1.0
 */
@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
  // Observe LiveData changes and convert them into state for Composable to react
  val currentProfileState by viewModel.profile.collectAsState()
  val isInEditMode by viewModel.isInEditMode.collectAsState(false)
  // If currentProfileState is null, do not proceed to render the UI
  val currentProfile: Profile = currentProfileState ?: return

  if (isInEditMode) {
    EditableVisitCard(
        profile = currentProfile,
        onProfileChange = { updatedProfile ->
          // Instead of changing local state, directly update the ViewModel
          viewModel.updateProfile(updatedProfile)
        },
        viewModel = viewModel)
  } else {
    Column(modifier = Modifier.fillMaxSize().testTag("profileScreen")) {
      Box(modifier = Modifier.fillMaxWidth()) {
        VisitCard(Modifier, currentProfile)
        ProfileSwitch(Modifier.align(Alignment.TopEnd), viewModel)
        ClickableIcon(Modifier.align(Alignment.BottomEnd), Icons.Filled.Create, viewModel)
      }
    }
  }
}

/**
 * This handle the logic behind the switch that can permit the user to switch to the anonymous mode
 *
 * @param modifier to place the switch at a place, and still be able to modify it.
 * @author Menzo Bouaissi
 * @since 1.0
 * @last update 1.0
 */
@Composable
fun ProfileSwitch(modifier: Modifier = Modifier, viewModel: ProfileViewModel) {
  // Determine the current public mode state
  val isPublicMode by viewModel.isInPublicMode.collectAsState(false)
  Switch(
      checked = isPublicMode,
      onCheckedChange = {
        // When the switch is toggled, call viewModel's method to update the profile's public mode
        viewModel.togglePublicMode()
      },
      modifier =
          modifier
              .graphicsLayer {
                scaleX = SCALE_X
                scaleY = SCALE_Y
              }
              .testTag("profileSwitch"),
      colors =
          SwitchDefaults.colors(
              checkedThumbColor = MaterialTheme.colorScheme.primary,
              checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
              uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
              uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
          ),
  )
}

/**
 * Handle the logic behind the edit button
 *
 * @param modifier the modifier to be applied to the Icon
 * @param icon the icon to be display
 * @param viewModel the viewModel that will handle the profile
 * @author Menzo Bouaissi
 * @since 1.0
 * @last update 1.0
 */
@Composable
fun ClickableIcon(
    modifier: Modifier,
    icon: ImageVector,
    viewModel: ProfileViewModel,
) {

  IconButton(
      modifier = modifier.then(Modifier.size(24.dp)).testTag("clickableIcon"),
      onClick = { viewModel.toggleEditMode() }) {
        Icon(
            icon,
            contentDescription = "Edit",
            modifier = modifier,
            // tint = if (isInEditMode) MaterialTheme.colorScheme.onSurface else
            // MaterialTheme.colorScheme.primary
        )
      }
}

/**
 * Display a visit card with the information of the user
 *
 * @param modifier the modifier to be applied to the Icon
 * @param profile the profile of the user
 * @author Menzo Bouaissi
 * @since 1.0
 * @last update 1.0
 */
@Composable
fun VisitCard(modifier: Modifier = Modifier, profile: Profile) {
  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.testTag("visitCard")) {
    SelectImage(
        modifier =
            modifier
                .padding(top = 48.dp, bottom = 48.dp, start = 16.dp, end = 0.dp)
                .size(width = 150.dp, height = 100.dp)
                .testTag("profilePicture"),
        profile = profile)

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(modifier = Modifier.testTag("outputFirstName"), text = profile.firstName)
      Text(modifier = Modifier.testTag("outputLastName"), text = profile.lastName)
      Text(modifier = Modifier.testTag("outputDescription"), text = profile.description)
      Text(text = profile.numberOfLikes.toString())
    }
  }
}

/**
 * Handle the logic of choosing an image from the phone
 *
 * @param profile the profile of the user
 * @param onImageChange enable to transmit the changed to the caller
 * @author Menzo Bouaissi
 * @since 1.0
 * @last update 1.0
 */
@Composable
fun ImageSelection(profile: Profile, onImageChange: (Uri?) -> Unit) {
  var imageUri by remember { mutableStateOf<Uri?>(null) }
  val launcher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri?
        ->
        imageUri = uri
        onImageChange(uri)
      }
  if (imageUri != null) profile.copy(profilePictureUri = imageUri)
  Box(modifier = Modifier.fillMaxWidth()) {
    SelectImage(
        modifier =
            Modifier.padding(top = 48.dp, bottom = 48.dp, start = 16.dp, end = 0.dp)
                .size(width = 150.dp, height = 100.dp)
                .clickable { launcher.launch("image/*") }
                .align(Alignment.Center)
                .testTag("profilePicture"),
        profile = profile)
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
fun ActionButtons(onSave: () -> Unit, onCancel: () -> Unit) {
  Column(
      verticalArrangement = Arrangement.spacedBy(2.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = onSave,
            colors = ButtonDefaults.buttonColors(containerColor = md_theme_light_primary),
            modifier = Modifier.width(100.dp).testTag("saveButton")) {
              Text("Save")
              // TODO: Send the data to the server
            }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, md_theme_light_error),
            modifier = Modifier.width(100.dp).testTag("cancelButton")) {
              Text(text = "Cancel", color = md_theme_light_error)
            }
      }
}

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
fun EditableVisitCard(
    profile: Profile,
    onProfileChange: (Profile) -> Unit,
    viewModel: ProfileViewModel
) {
  val profile2 = profile.copy()
  var firstName by remember { mutableStateOf(profile2.firstName) }
  var lastName by remember { mutableStateOf(profile2.lastName) }
  var description by remember { mutableStateOf(profile2.description) }

  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    ImageSelection(profile = profile2, onImageChange = { uri -> profile2.profilePictureUri = uri })
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
          viewModel.toggleEditMode()
          onProfileChange(
              profile.copy(
                  firstName = firstName,
                  lastName = lastName,
                  description = description,
                  profilePictureUri = profile2.profilePictureUri))
        },
        onCancel = { viewModel.toggleEditMode() })
  }
}

/**
 * Handle the logic behind the image, when the user has a profile picture, we display it via
 * AsynImage, else we just display a based image with Image
 *
 * @param modifier the modifier to be applied to the Icon
 * @param profile the profile of the user
 * @author Menzo Bouaissi
 * @since 1.0
 * @last update 1.0
 */
@Composable
fun SelectImage(modifier: Modifier, profile: Profile) {
  if (profile.profilePictureUri != null) {
    AsyncImage(
        model = profile.profilePictureUri,
        contentDescription = "Profile picture",
        modifier = modifier)
  } else {
    Image(
        painter = painterResource(id = R.drawable.profile_picture),
        contentDescription = "Profile picture",
        modifier = modifier)
  }
}

/**
 * For debug use, to see the screen
 *
 * @author Menzo Bouaissi
 * @since 1.0
 * @last update 1.0
 */
@Preview
@Composable
fun ProfileScreenPreview() {
  val vm = ProfileViewModel()
  ProfileScreen(vm)
}
