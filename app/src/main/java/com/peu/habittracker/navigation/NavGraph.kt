package com.peu.habittracker.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.peu.habittracker.screen.AddHabitScreen
import com.peu.habittracker.screen.HomeScreen
import com.peu.habittracker.screen.HabitDetailScreen // Add this import

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddHabit : Screen("add_habit")
    object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: Long) = "habit_detail/$habitId"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAddHabit = {
                    navController.navigate(Screen.AddHabit.route)
                },
                onNavigateToDetail = { habitId ->
                    navController.navigate(Screen.HabitDetail.createRoute(habitId))
                }
            )
        }

        composable(Screen.AddHabit.route) {
            AddHabitScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- ADDED THIS BLOCK TO FIX THE CRASH ---
        composable(
            route = Screen.HabitDetail.route,
            arguments = listOf(
                navArgument("habitId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId") ?: -1L
            HabitDetailScreen(
                habitId = habitId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}