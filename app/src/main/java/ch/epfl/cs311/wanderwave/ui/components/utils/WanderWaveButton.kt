package ch.epfl.cs311.wanderwave.ui.components.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.epfl.cs311.wanderwave.R
import ch.epfl.cs311.wanderwave.ui.theme.md_theme_dark_error

@Composable
fun WanderWaveButton(
    id: Int,
    onClick: () -> Unit,
    modifier: Modifier,
    textColor: Color = Color.Transparent,
    borderColor: Color = Color.Transparent,
    containerColor: Color= Color.Transparent
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(size = 10.dp),
        modifier = modifier) {
        Text(text = stringResource(id = id), color = textColor)
    }
}