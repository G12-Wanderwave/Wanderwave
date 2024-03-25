package ch.epfl.cs311.wanderwave.ui.screens

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
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

    Column(
        modifier = Modifier
            .fillMaxSize()
    ){
        Box(modifier = Modifier
            .fillMaxWidth()) {


            if( !isInEditMode){
               VisitCard(Modifier,currentProfile)
            }else{
                EditableVisitCard(
                    modifier = Modifier,
                    profile = currentProfile,
                    onProfileChange = { updatedProfile ->
                        currentProfile = updatedProfile
                        // Additional logic to handle the updated profile can be added here.
                    }
                )

           }
            ProfileSwitch(Modifier.align(Alignment.TopEnd))
            ClickableIcon(Modifier.align(Alignment.BottomEnd), isInEditMode){ value ->
                isInEditMode = value
            }
        }
    }
}

@Composable
fun GetPicture(profile:Profile){

    Button(onClick = {
    }) {
        Text(text = "select image")
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
                  isInEditMode: Boolean,
                  onModeChange: (Boolean) -> Unit){

    IconButton(modifier = modifier.
    then(Modifier.size(24.dp)),
        onClick = { onModeChange(!isInEditMode)
        //TODO: add the fact to send to the DB the new updated profile
        }) {
        Icon(Icons.Filled.Create,
            contentDescription = "Edit",
            modifier = modifier,
            tint = if (isInEditMode)  MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
        )

    }


}
@Composable
fun VisitCard(modifier: Modifier = Modifier,profile: Profile){
    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = profile.profilePictureUri,//painterResource(id =profile.profilePictureResId),
              contentDescription = "profile picture",
              modifier = modifier
                  .padding(top = 48.dp, bottom = 48.dp, start = 16.dp, end = 16.dp)
                  .size(width = 150.dp, height = 100.dp)
                  .fillMaxWidth()
                  .testTag("profilePicture"))

        /*AsyncImage(
            model = profile.profilePictureUri,
            contentDescription = null,
            modifier = Modifier
                //.padding(4.dp)
                //.width(100.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
        )*/
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
    profile.profilePictureUri = imageUri

    Row() {
        AsyncImage(
            model = profile.profilePictureUri,//painterResource(id = ),
            contentDescription = "Profile picture",
            modifier = modifier
                .padding(top = 48.dp, bottom = 48.dp, start = 16.dp, end = 0.dp)
                .size(width = 150.dp, height = 100.dp)
                .clickable {
                    launcher.launch("image/*")

                    /*val newProfile =
                        if (profile.profilePictureResId == R.drawable.profile_picture) {
                            profile.copy(profilePictureResId = R.drawable.new_profile)
                        } else {
                            profile.copy(profilePictureResId = R.drawable.profile_picture)
                        }
                    onProfileChange(newProfile)
                    */
                }
                .testTag("profilePicture"),

        )
/*
        AsyncImage(
            model = profile.profilePictureResId,
            contentDescription = null,
            modifier = Modifier
                //.padding(4.dp)
                //.width(100.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
        )*/
        Column( modifier = modifier
            .padding(vertical = 50.dp)
        ){
            SmallTextField(
                value = firstName,
                onValueChange = { newName ->
                    firstName = newName
                    onProfileChange(profile.copy(firstName = newName))
                },
                label = { Text("First Name") }
            )
            SmallTextField(
                value = lastName,
                onValueChange = { newName ->
                    lastName = newName
                    onProfileChange(profile.copy(lastName = newName))
                },
                label = { Text("Last Name") }
            )
            SmallTextField(
                value = description,
                onValueChange = { newDescription ->
                    description = newDescription
                    onProfileChange(profile.copy(description = newDescription))
                },
                label = { Text("Description") }
            )

        }
    }
}

@Composable
fun SmallTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        //label = label,
        modifier = Modifier
            .height(IntrinsicSize.Min)

            //.fillMaxWidth()
            //.height(30.dp)
    )
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