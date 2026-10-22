package com.example.rabbittracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

val habitIcons: List<String> = listOf(
    "water_drop", "directions_walk", "menu_book", "fitness_center", "self_improvement", "hotel",
    "spa", "directions_run", "edit_note", "wb_sunny", "psychology", "restaurant",
    "schedule", "palette", "code", "headphones", "attach_money", "home",
    "sentiment_satisfied", "eco", "laptop_mac", "nightlight", "check_circle", "favorite"
)

fun habitIcon(name: String): ImageVector = when (name) {
    "water_drop" -> Icons.Rounded.WaterDrop
    "directions_walk" -> Icons.Rounded.DirectionsWalk
    "menu_book" -> Icons.Rounded.Book
    "fitness_center" -> Icons.Rounded.FitnessCenter
    "self_improvement" -> Icons.Rounded.Spa
    "hotel" -> Icons.Rounded.Hotel
    "spa" -> Icons.Rounded.Spa
    "directions_run" -> Icons.Rounded.DirectionsRun
    "edit_note" -> Icons.Rounded.EditNote
    "wb_sunny" -> Icons.Rounded.WbSunny
    "psychology" -> Icons.Rounded.Psychology
    "restaurant" -> Icons.Rounded.Restaurant
    "schedule" -> Icons.Rounded.Schedule
    "palette" -> Icons.Rounded.Palette
    "code" -> Icons.Rounded.Code
    "headphones" -> Icons.Rounded.HeadsetMic
    "attach_money" -> Icons.Rounded.AttachMoney
    "home" -> Icons.Rounded.Home
    "sentiment_satisfied" -> Icons.Rounded.SentimentSatisfied
    "eco" -> Icons.Rounded.Eco
    "laptop_mac" -> Icons.Rounded.LaptopMac
    "nightlight" -> Icons.Rounded.Nightlight
    "check_circle" -> Icons.Rounded.CheckCircle
    "favorite" -> Icons.Rounded.Favorite
    else -> Icons.Rounded.CheckCircle
}

val habitAccentNames: List<String> = listOf("peach", "light_blue", "mint", "grey", "brown", "tan")

@Composable
fun habitAccent(name: String): Color {
    return MaterialTheme.colorScheme.surfaceContainerHigh
}

val habitAccentColors: List<Color> = listOf(
    Color(0xFFE86A45),
    Color(0xFF2E7D32),
    Color(0xFF2196F3),
    Color(0xFF8B6F47),
    Color(0xFF607D8B)
)
