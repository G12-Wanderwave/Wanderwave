package ch.epfl.cs311.wanderwave.ui.components.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.R

@Composable
fun LoginScreenHeader(modifier: Modifier) {

  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Icon(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "app logo",
            tint = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier.height(50.dp)
                    .fillMaxWidth()
                    .padding(start = 50.dp, end = 50.dp)
                    .testTag("appLogo"))
      }
}
