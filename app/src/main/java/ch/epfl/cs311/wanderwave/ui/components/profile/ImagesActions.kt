package ch.epfl.cs311.wanderwave.ui.components.profile

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
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
  Log.d("SelectImage", profile.profilePictureUri.toString())
  if (profile.profilePictureUri != null && profile.profilePictureUri.toString() != "") {
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
  // var imageUri by remember { mutableStateOf<Uri?>(null) }
  val launcher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri?
        ->
        if (uri != null) profile.copy(profilePictureUri = uri)
        onImageChange(uri)
      }
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
