package ch.epfl.cs311.wanderwave.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.R

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ){
        Box(modifier = Modifier
            .fillMaxWidth()) {
            visitCard(Modifier)
            profileSwitch(Modifier.align(Alignment.TopEnd))
            clickableIcon(Modifier.align(Alignment.BottomEnd))
        }
    }
}
@Composable
fun profileSwitch(modifier: Modifier = Modifier){
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
            scaleX = 0.5f // 1.0f is the original size, 1.5f is 50% larger
            scaleY = 0.5f
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
fun clickableIcon(modifier: Modifier){
    var isInEditMode by remember { mutableStateOf(false) }

    IconButton(modifier = modifier.
    then(Modifier.size(24.dp)),
        onClick = { isInEditMode = !isInEditMode}){
        Icon(Icons.Filled.Create,
            contentDescription = "Edit",
            modifier = modifier,
            tint = if (isInEditMode)  MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
        )

    }


}
@Composable
fun visitCard(modifier: Modifier = Modifier){
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(painter = painterResource(id = R.drawable.profile_picture),
              contentDescription = "profile picture",
              modifier = modifier
                  .padding(top = 48.dp, bottom = 48.dp, start = 16.dp, end = 16.dp)
                  .size(width = 150.dp, height = 100.dp)
                  .fillMaxWidth()
                  .testTag("profilePicture"))
        Column {
            Text("First Name2")
            Text("Last Name")
            Text("Description")
            Text("# of likes")

        }
    }
}
@Preview
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}