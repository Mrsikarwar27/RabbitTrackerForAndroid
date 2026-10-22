package com.example.rabbittracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Today : Screen("today", "Today", Icons.Rounded.Home)
    object Habits : Screen("habits", "Habits", Icons.Rounded.FormatListBulleted)
    object Statistics : Screen("statistics", "Statistics", Icons.Rounded.Insights)
    object Calendar : Screen("calendar", "Calendar", Icons.Rounded.CalendarToday)
}
