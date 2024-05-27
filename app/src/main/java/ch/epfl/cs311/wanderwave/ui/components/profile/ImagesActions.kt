package ch.epfl.cs311.wanderwave.ui.components.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.model.data.Profile
import ch.epfl.cs311.wanderwave.ui.theme.ProfileBackground1
import ch.epfl.cs311.wanderwave.ui.theme.ProfileBackground10
import ch.epfl.cs311.wanderwave.ui.theme.ProfileBackground11
import ch.epfl.cs311.wanderwave.ui.theme.ProfileBackground12
import ch.epfl.cs311.wanderwave.ui.theme.ProfileBackground13
import ch.epfl.cs311.wanderwave.ui.theme.ProfileBackground14
import ch.epfl.cs311.wanderwave.ui.theme.ProfileBackground15
import ch.epfl.cs311.wanderwave.ui.theme.ProfileBackground16
import ch.epfl.cs311.wanderwave.ui.theme.ProfileBackground17
import ch.epfl.cs311.wanderwave.ui.theme.ProfileBackground18
import ch.epfl.cs311.wanderwave.ui.theme.ProfileBackground19
import ch.epfl.cs311.wanderwave.ui.theme.ProfileBackground2
import ch.epfl.cs311.wanderwave.ui.theme.ProfileBackground20
import ch.epfl.cs311.wanderwave.ui.theme.ProfileBackground21
import ch.epfl.cs311.wanderwave.ui.theme.ProfileBackground3
import ch.epfl.cs311.wanderwave.ui.theme.ProfileBackground4
import ch.epfl.cs311.wanderwave.ui.theme.ProfileBackground5
import ch.epfl.cs311.wanderwave.ui.theme.ProfileBackground6
import ch.epfl.cs311.wanderwave.ui.theme.ProfileBackground7
import ch.epfl.cs311.wanderwave.ui.theme.ProfileBackground8
import ch.epfl.cs311.wanderwave.ui.theme.ProfileBackground9
import kotlin.math.abs

/**
 * Handle the logic behind the image, when the user has a profile picture, we display it via
 * AsynImage, else we just display a based image with Image
 *
 * @param modifier the modifier to be applied to the Icon
 * @param profile the profile of the user
 * @author Menzo Bouaissi
 * @since 1.0
 * @last update 3.0
 */
@Composable
fun SelectImage(modifier: Modifier, imageUri: Uri?, profile: Profile) {
  // if (imageUri != null) {
  // AsyncImage(model = imageUri, contentDescription = "Profile picture", modifier = modifier)
  // } else {
  PlaceholderProfilePicture(
      name = profile.firstName,
      modifier =
          modifier
              .size(48.dp) // Ensure the placeholder is circular
              .background(MaterialTheme.colorScheme.primary, CircleShape))
  // }
}

/**
 * Handle the logic of choosing an image from the phone
 *
 * @param profile the profile of the user
 * @param onImageChange enable to transmit the changed to the caller
 * @author Menzo Bouaissi
 * @since 1.0
 * @last update 3.0
 */
@Composable
fun ImageSelection(profile: Profile, onImageChange: (Uri?) -> Unit) {
  var imageUri by remember { mutableStateOf(profile.profilePictureUri) }

  val launcher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri?
        ->
        imageUri = uri
        onImageChange(uri)
      }

  Box(modifier = Modifier.fillMaxWidth()) {
    // Pass `imageUri` directly to `AsyncImage`
    SelectImage(
        modifier =
            Modifier.padding(top = 48.dp, bottom = 48.dp, start = 16.dp, end = 0.dp)
                .size(width = 100.dp, height = 100.dp)
                // .clickable { launcher.launch("image/*") }
                .align(Alignment.Center)
                .testTag("profilePicture"),
        imageUri = imageUri,
        profile = profile)
  }
}

/**
 * Display a letter instead of a profile picture
 *
 * @param modifier the modifier to be applied to the Icon
 * @param name the name of the user
 */
@Composable
fun PlaceholderProfilePicture(name: String, modifier: Modifier = Modifier) {
  // Generate a color based on the hash of the name
  val colors =
      listOf(
          ProfileBackground1,
          ProfileBackground2,
          ProfileBackground3,
          ProfileBackground4,
          ProfileBackground5,
          ProfileBackground6,
          ProfileBackground7,
          ProfileBackground8,
          ProfileBackground9,
          ProfileBackground10,
          ProfileBackground11,
          ProfileBackground12,
          ProfileBackground13,
          ProfileBackground14,
          ProfileBackground15,
          ProfileBackground16,
          ProfileBackground17,
          ProfileBackground18,
          ProfileBackground19,
          ProfileBackground20,
          ProfileBackground21)
  val backgroundColor = colors[abs(name.hashCode() % colors.size)]

  Box(
      contentAlignment = Alignment.Center,
      modifier =
          modifier
              .background(backgroundColor, shape = CircleShape)
              .size(48.dp) // Ensure the size is equal in both dimensions
      ) {
        Text(
            text = name.firstOrNull()?.toUpperCase()?.toString() ?: "",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium)
      }
}
