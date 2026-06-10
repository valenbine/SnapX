package com.example.snapx.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons.Rounded
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.snapx.ui.edit.EditScreen
import com.example.snapx.ui.floating.FloatingWindowManager
import com.example.snapx.ui.history.HistoryScreen
import com.example.snapx.ui.screenshot.ScreenshotScreen
import com.example.snapx.ui.longshot.LongScreenshotScreen
import com.example.snapx.ui.settings.SettingsScreen

sealed class Screen(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Screenshot : Screen("screenshot", "截图", Icons.Default.PhotoCamera)
    object LongScreenshot : Screen("long_screenshot", "长截图", Icons.Default.ViewStream)
    object History : Screen("history", "历史", Icons.Default.History)
    object Settings : Screen("settings", "设置", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val screens = listOf(
                    Screen.Screenshot,
                    Screen.LongScreenshot,
                    Screen.History,
                    Screen.Settings
                )
                
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Screenshot.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Screenshot.route) {
                ScreenshotScreen(navController)
            }
            composable(Screen.LongScreenshot.route) {
                LongScreenshotScreen(navController)
            }
            composable(Screen.History.route) {
                HistoryScreen(navController)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(navController)
            }
            composable("edit/{screenshotId}") { backStackEntry ->
                val screenshotId = backStackEntry.arguments?.getString("screenshotId")?.toLongOrNull()
                EditScreen(navController, screenshotId)
            }
        }
    }
}