package com.peu.habittracker.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.peu.habittracker.screen.AddHabitScreen
import com.peu.habittracker.screen.HomeScreen
import com.peu.habittracker.screen.HabitDetailScreen // Add this import
import com.peu.habittracker.screen.SettingsScreen
import com.peu.habittracker.screen.StatisticsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddHabit : Screen("add_habit")
    object EditHabit : Screen("edit_habit/{habitId}") {
        fun createRoute(habitId: Long) = "edit_habit/$habitId"
    }
    object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: Long) = "habit_detail/$habitId"
    }
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // Home Screen with fade animation
        composable(
            route = Screen.Home.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            HomeScreen(
                onNavigateToAddHabit = {
                    navController.navigate(Screen.AddHabit.route)
                },
                onNavigateToDetail = { habitId ->
                    navController.navigate(Screen.HabitDetail.createRoute(habitId))
                },
                onNavigateToStatistics = {
                    navController.navigate(Screen.Statistics.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // Add Habit Screen with slide up animation
        composable(
            route = Screen.AddHabit.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(400)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(400)
                )
            }
        ) {
            AddHabitScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Edit Habit Screen
        composable(
            route = Screen.EditHabit.route,
            arguments = listOf(
                navArgument("habitId") { type = NavType.LongType }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(400)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(400)
                )
            }
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId") ?: -1L
            AddHabitScreen(
               // habitId = habitId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Habit Detail Screen with shared element animation
        composable(
            route = Screen.HabitDetail.route,
            arguments = listOf(
                navArgument("habitId") { type = NavType.LongType }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(400)
                ) + fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(400))
            }
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId") ?: -1L
            HabitDetailScreen(
                habitId = habitId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = {
                    navController.navigate(Screen.EditHabit.createRoute(habitId))
                }
            )
        }

        // Statistics Screen
        composable(
            route = Screen.Statistics.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(400)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(400)
                )
            }
        ) {
            StatisticsScreen (
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Settings Screen
        composable(
            route = Screen.Settings.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(400)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(400)
                )
            }
        ) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
