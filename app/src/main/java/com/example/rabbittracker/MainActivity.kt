package com.example.rabbittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.rabbittracker.ui.AppNavigation
import com.example.rabbittracker.ui.HabitViewModel
import com.example.rabbittracker.ui.components.BottomNav
import com.example.rabbittracker.ui.components.Screen
import com.example.rabbittracker.ui.theme.RabbitTrackerTheme

class MainActivity : ComponentActivity() {
    private var viewModelRef: HabitViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            val habitViewModel: HabitViewModel = viewModel()
            viewModelRef = habitViewModel

            RabbitTrackerTheme {
                val navController = rememberNavController()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Today.route

                val navigateToTab: (String) -> Unit = { route ->
                    if (route != currentRoute) {
                        val popped = navController.popBackStack(route, inclusive = false)
                        if (!popped) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomNav(
                            currentRoute = currentRoute,
                            onNavigate = navigateToTab
                        )
                    },
                    floatingActionButton = {
                        val showFab = currentRoute == Screen.Today.route ||
                            currentRoute == Screen.Habits.route
                        if (showFab) {
                            ExtendedFloatingActionButton(
                                onClick = {
                                    navController.navigate("create_habit") {
                                        launchSingleTop = true
                                    }
                                },
                                text = {
                                    Text(
                                        text = "New Habit",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = "New habit",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                },
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                shape = FloatingActionButtonDefaults.extendedFabShape
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AppNavigation(
                            navController = navController,
                            viewModel = habitViewModel
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModelRef?.refresh()
    }
}
