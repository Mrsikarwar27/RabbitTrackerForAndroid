package com.example.rabbittracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.rabbittracker.ui.components.Screen
import com.example.rabbittracker.ui.screens.*

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: HabitViewModel
) {
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsStateWithLifecycle()

    if (onboardingCompleted == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
        return
    }

    NavHost(
        navController = navController,
        startDestination = if (onboardingCompleted == true) Screen.Today.route else "onboarding"
    ) {
        composable("onboarding") {
            OnboardingScreen(
                viewModel = viewModel,
                onFinished = {
                    navController.navigate(Screen.Today.route) {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Today.route) {
            TodayScreen(
                viewModel = viewModel,
                onNavigateToHabits = { navController.navigate(Screen.Habits.route) },
                onNavigateToCreate = { navController.navigate("create_habit") },
                onNavigateToDetail = { id -> navController.navigate("habit_detail/$id") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable(Screen.Habits.route) {
            HabitsScreen(
                viewModel = viewModel,
                onNavigateToCreate = { navController.navigate("create_habit") },
                onNavigateToDetail = { id -> navController.navigate("habit_detail/$id") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable(Screen.Statistics.route) {
            StatisticsScreen(
                viewModel = viewModel,
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable(Screen.Calendar.route) {
            CalendarScreen(
                viewModel = viewModel,
                onNavigateToCreate = { navController.navigate("create_habit") },
                onNavigateToDetail = { id -> navController.navigate("habit_detail/$id") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "create_habit?edit={habitId}",
            arguments = listOf(
                navArgument("habitId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val editId = backStackEntry.arguments?.getInt("habitId") ?: -1
            CreateHabitScreen(
                viewModel = viewModel,
                editHabitId = editId.takeIf { it > 0 },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "habit_detail/{habitId}",
            arguments = listOf(
                navArgument("habitId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("habitId") ?: 0
            HabitDetailScreen(
                viewModel = viewModel,
                habitId = id,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate("create_habit?edit=$id") }
            )
        }
    }
}
