package ch.epfl.cs311.wanderwave.ui.screens

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.model.data.Profile
import coil.compose.AsyncImage

const val SCALE_X = 0.5f
const val SCALE_Y = 0.5f
const val SCALE_X_TEXTFIELD = 1f
const val SCALE_Y_TEXTFIELD = 1f
@Composable
fun ProfileScreen(profile:Profile) {
    var isInEditMode by remember { mutableStateOf(false) }
    var currentProfile by remember { mutableStateOf(profile) }

    if (isInEditMode){

        EditableVisitCard(
            profile = currentProfile,
            onProfileChange = { updatedProfile ->
                currentProfile = updatedProfile
                // Additional logic to handle the updated profile can be added here.
            }
        )
        ClickableIcon(Modifier,Icons.Filled.ExitToApp, isInEditMode){ value ->
            isInEditMode = value
        }
    }else{

        Column(
            modifier = Modifier
                .fillMaxSize()
        ){
            Box(modifier = Modifier
                .fillMaxWidth()) {
                VisitCard(Modifier,currentProfile)
                ProfileSwitch(Modifier.align(Alignment.TopEnd))
                ClickableIcon(Modifier.align(Alignment.BottomEnd),Icons.Filled.Create, isInEditMode){ value ->
                    isInEditMode = value
                }
            }
        }

    }

}
@Composable
fun ProfileSwitch(modifier: Modifier = Modifier ){
    var isInPublicMode by remember { mutableStateOf(true) }

    Switch(
        checked = isInPublicMode,
        onCheckedChange = { //
            //TODO: add the fact that we are going anonymous,
            //This could be done by just settings a certain value in the profile class to 1
            isInPublicMode = !isInPublicMode
                          },
        modifier = modifier
            .graphicsLayer{
            // Replace scaleX and scaleY with your desired scale factors
            scaleX = SCALE_X // 1.0f is the original size, 1.5f is 50% larger
            scaleY = SCALE_Y
        },
        //TODO: Look again at the color of the theme
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.primary,
            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
            uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
            uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
        ),

        )
}
@Composable
fun ClickableIcon(modifier: Modifier,
                  icon : ImageVector,
                  isInEditMode: Boolean,
                  onModeChange: (Boolean) -> Unit){

    IconButton(modifier = modifier.
    then(Modifier.size(24.dp)),
        onClick = { onModeChange(!isInEditMode)
        //TODO: add the fact to send to the DB the new updated profile
        }) {
        Icon(icon,
            contentDescription = "Edit",
            modifier = modifier,
            tint = if (isInEditMode)  MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
        )

    }


}
@Composable
fun VisitCard(modifier: Modifier = Modifier,profile: Profile){
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (profile.profilePictureUri != null) {
            AsyncImage(
                model = profile.profilePictureUri,
                contentDescription = "Profile picture",
                modifier = modifier
                    .padding(top = 48.dp, bottom = 48.dp, start = 16.dp, end = 0.dp)
                    .size(width = 150.dp, height = 100.dp)
                    .testTag("profilePicture")
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.profile_picture),
                contentDescription = "Profile picture",
                modifier = modifier
                    .padding(top = 48.dp, bottom = 48.dp, start = 16.dp, end = 0.dp)
                    .size(width = 150.dp, height = 100.dp)
                    .testTag("profilePicture")
            )
        }
        Column {
            Text(profile.firstName)
            Text(profile.lastName)
            Text(profile.description)
            Text(profile.numberOfLikes.toString())

        }
    }
}


@Composable
fun EditableVisitCard(modifier: Modifier = Modifier, profile: Profile, onProfileChange: (Profile) -> Unit) {
    var firstName by remember { mutableStateOf(profile.firstName) }
    var lastName by remember { mutableStateOf(profile.lastName) }
    var description by remember { mutableStateOf(profile.description) }

    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val launcher = rememberLauncherForActivityResult(
        contract =
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }
    if(imageUri!=null)profile.profilePictureUri=imageUri

    Column (){
        Box(modifier = Modifier.fillMaxWidth()) {
            if (profile.profilePictureUri != null) {
                AsyncImage(
                    model = profile.profilePictureUri,
                    contentDescription = "Profile picture",
                    modifier = modifier
                        .padding(top = 48.dp, bottom = 48.dp, start = 16.dp, end = 0.dp)
                        .size(width = 150.dp, height = 100.dp)
                        .clickable {
                            launcher.launch("image/*")
                        }
                        .align(Alignment.Center) // Center horizontally and vertically inside the Box
                    .testTag("profilePicture")
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.profile_picture),
                    contentDescription = "Profile picture",
                    modifier = modifier
                        .padding(top = 48.dp, bottom = 48.dp, start = 16.dp, end = 0.dp)
                        .size(width = 150.dp, height = 100.dp)
                        .clickable {
                            launcher.launch("image/*")
                        }
                        .align(Alignment.Center)
                        .testTag("profilePicture")
                )
            }
        }


        Row(horizontalArrangement = Arrangement.Center){
            Column (
                modifier= Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally// Spacing between items
            ){
                Row(

                ){

                    SmallTextField(
                        value = firstName,
                        modifier = Modifier
                            .width(150.dp),
                        onValueChange = { newName ->
                            firstName = newName
                            onProfileChange(profile.copy(firstName = newName))
                        },
                        label = { Text("First Name") }
                    )
                    SmallTextField(
                        value = lastName,
                        modifier = Modifier
                            .width(150.dp),
                        onValueChange = { newName ->
                            lastName = newName
                            onProfileChange(profile.copy(lastName = newName))
                        },
                        label = { Text("Last Name") }
                    )
                }
                SmallTextField(
                    value = description,
                    modifier = Modifier,
                    onValueChange = { newDescription ->
                        description = newDescription
                        onProfileChange(profile.copy(description = newDescription))
                    },
                    label = { Text("Description") }
                )

            }

        }
    }
}



@Composable
fun SmallTextField(
    value: String,
    modifier: Modifier,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        //label = label,
        modifier = modifier
            .height(IntrinsicSize.Min)
            .padding(horizontal = 8.dp)
    )
}

@Composable
fun SelectImage(){


}
@Preview
@Composable
fun ProfileScreenPreview() {
    var profile: Profile by remember{
        mutableStateOf(
            Profile(
            firstName = "My Name",
            lastName = "My Name",
            description = "My Description",
            numberOfLikes = 0,
            isPublic = true,
            profilePictureUri = null
            )
        )
    }

    ProfileScreen(profile)
}