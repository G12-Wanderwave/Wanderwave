package ch.epfl.cs311.wanderwave.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.model.data.Profile
import coil.compose.AsyncImage

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
