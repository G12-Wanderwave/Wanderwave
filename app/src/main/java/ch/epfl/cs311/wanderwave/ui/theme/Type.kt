package ch.epfl.cs311.wanderwave.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ch.epfl.cs311.wanderwave.R

// Set of Material typography styles to start with

val Ubuntu =
    FontFamily(
        Font(resId = R.font.ubuntu_regular, weight = FontWeight.Normal),
        Font(resId = R.font.ubuntu_medium, weight = FontWeight.Medium),
        Font(resId = R.font.ubuntu_bold, weight = FontWeight.Bold),
        Font(resId = R.font.ubuntu_light, weight = FontWeight.Light),
        Font(resId = R.font.ubuntu_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
        Font(
            resId = R.font.ubuntu_medium_italic,
            weight = FontWeight.Medium,
            style = FontStyle.Italic),
        Font(resId = R.font.ubuntu_bold_italic, weight = FontWeight.Bold, style = FontStyle.Italic),
        Font(
            resId = R.font.ubuntu_light_italic,
            weight = FontWeight.Light,
            style = FontStyle.Italic))

val Typography =
    Typography(
        displayLarge =
            TextStyle(
                fontFamily = Ubuntu,
                fontWeight = FontWeight.Bold,
                fontSize = 50.sp,
                lineHeight = 40.sp,
                letterSpacing = 0.15.sp),
        displayMedium =
            TextStyle(
                fontFamily = Ubuntu,
                fontWeight = FontWeight.Bold,
                fontSize = 35.sp,
                lineHeight = 40.sp,
                letterSpacing = 0.15.sp),
        bodyLarge =
            TextStyle(
                fontFamily = Ubuntu,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp),
        titleMedium =
            TextStyle(
                fontFamily = Ubuntu,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.sp),
        titleSmall =
            TextStyle(
                fontFamily = Ubuntu,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.sp)
        /* Other default text styles to override
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
        */
        )
