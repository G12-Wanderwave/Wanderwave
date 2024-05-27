package ch.epfl.cs311.wanderwave.ui.components.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.model.data.Profile

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
  Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(12.dp).testTag("visitCard")) {
        SelectImage(
            modifier =
                modifier
                    .padding(top = 48.dp, bottom = 48.dp, start = 16.dp, end = 0.dp)
                    .size(width = 100.dp, height = 100.dp)
                    .testTag("profilePicture"),
            imageUri = profile.profilePictureUri,
            profile = profile)
        Spacer(modifier = Modifier.width(16.dp)) // Adjust the width for more space
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
