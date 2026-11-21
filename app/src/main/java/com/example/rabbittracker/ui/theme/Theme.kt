package com.example.rabbittracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

val RabbitShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp), // habit tiles 16dp rounded squares
    large = RoundedCornerShape(32.dp),   // cards 32dp corner radius
    extraLarge = RoundedCornerShape(50.dp) // fully rounded pills
)

fun lightColorSchemeForAccent(accent: String) = lightColorScheme(
    primary = getLightPrimary(accent),
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = getLightPrimaryContainer(accent),
    onPrimaryContainer = androidx.compose.ui.graphics.Color.White,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceContainerHighLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight
)

fun darkColorSchemeForAccent(accent: String) = darkColorScheme(
    primary = getDarkPrimary(accent),
    onPrimary = androidx.compose.ui.graphics.Color.Black,
    primaryContainer = getDarkPrimaryContainer(accent),
    onPrimaryContainer = androidx.compose.ui.graphics.Color.White,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceContainerHighDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark
)

@Composable
fun RabbitTrackerTheme(
    themeMode: String = "system",
    accentColor: String = "Carrot",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) {
        darkColorSchemeForAccent(accentColor)
    } else {
        lightColorSchemeForAccent(accentColor)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = RabbitShapes,
        content = content
    )
}
