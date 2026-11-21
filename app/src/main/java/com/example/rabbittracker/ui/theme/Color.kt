package com.example.rabbittracker.ui.theme

import androidx.compose.ui.graphics.Color

// Base Light Scheme Tokens
val BackgroundLight = Color(0xFFFDF9F3)
val SurfaceLight = Color(0xFFFDF9F3)
val CardLight = Color(0xFFFFFFFF)
val SurfaceContainerLight = Color(0xFFF2EDE8)
val SurfaceContainerLowLight = Color(0xFFF8F3ED)
val SurfaceContainerHighLight = Color(0xFFE8E2DD)
val OnSurfaceLight = Color(0xFF1C1B18)
val OnSurfaceVariantLight = Color(0xFF5B403A)

val SecondaryLight = Color(0xFF006B5C)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFF9CF3DF)
val OnSecondaryContainerLight = Color(0xFF00201A)

val TertiaryLight = Color(0xFF845400)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryFixedLight = Color(0xFFFFDDB6)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)

// Base Dark Scheme Tokens (Warm Charcoal)
val BackgroundDark = Color(0xFF141210)
val SurfaceDark = Color(0xFF141210)
val CardDark = Color(0xFF1E1A18)
val SurfaceContainerDark = Color(0xFF231F1D)
val SurfaceContainerLowDark = Color(0xFF191513)
val SurfaceContainerHighDark = Color(0xFF2B2522)
val OnSurfaceDark = Color(0xFFE6E1DC)
val OnSurfaceVariantDark = Color(0xFFD2C0BA)

val SecondaryDark = Color(0xFF5CD6BE)
val OnSecondaryDark = Color(0xFF00382E)
val SecondaryContainerDark = Color(0xFF005144)
val OnSecondaryContainerDark = Color(0xFF9CF3DF)

val TertiaryDark = Color(0xFFFFB84D)
val OnTertiaryDark = Color(0xFF472A00)
val TertiaryFixedDark = Color(0xFF5C3A00)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)

// Accent Colors mapping
fun getLightPrimary(accent: String): Color = when (accent) {
    "Teal" -> Color(0xFF004D40)
    "Amber" -> Color(0xFF664100)
    "Purple" -> Color(0xFF5B21B6)
    "Rose" -> Color(0xFF9F1239)
    else -> Color(0xFFB52603) // Carrot
}

fun getLightPrimaryContainer(accent: String): Color = when (accent) {
    "Teal" -> Color(0xFF006B5C)
    "Amber" -> Color(0xFF845400)
    "Purple" -> Color(0xFF7C3AED)
    "Rose" -> Color(0xFFE11D48)
    else -> Color(0xFFFF5A36) // Carrot
}

fun getDarkPrimary(accent: String): Color = when (accent) {
    "Teal" -> Color(0xFF80CBC4)
    "Amber" -> Color(0xFFFFB84D)
    "Purple" -> Color(0xFFC4B5FD)
    "Rose" -> Color(0xFFFDA4AF)
    else -> Color(0xFFFFB5A0) // Carrot
}

fun getDarkPrimaryContainer(accent: String): Color = when (accent) {
    "Teal" -> Color(0xFF00897B)
    "Amber" -> Color(0xFFFFA000)
    "Purple" -> Color(0xFF9333EA)
    "Rose" -> Color(0xFFF43F5E)
    else -> Color(0xFF812A09) // Carrot
}
