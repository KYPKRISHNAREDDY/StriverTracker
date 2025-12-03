package com.dsatracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dsatracker.ui.screens.*

/**
 * Main navigation graph for the app
 * Defines all navigation routes and their destinations
 */
@Composable
fun DSANavGraph(
    navController: NavHostController,
    startDestination: String = Routes.Home.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Home Screen
        composable(Routes.Home.route) {
            HomeScreen(navController = navController)
        }

        // Sheet Overview with sheetId parameter
        composable(
            route = Routes.SheetOverview.route,
            arguments = listOf(
                navArgument("sheetId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sheetId = backStackEntry.arguments?.getString("sheetId") ?: ""
            SheetOverviewScreen(
                sheetId = sheetId,
                navController = navController
            )
        }

        // Topic Problems with topicId parameter
        composable(
            route = Routes.TopicProblems.route,
            arguments = listOf(
                navArgument("topicId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getLong("topicId") ?: 0L
            TopicProblemsScreen(
                topicId = topicId,
                navController = navController
            )
        }

        // Problem Detail with problemId parameter
        composable(
            route = Routes.ProblemDetail.route,
            arguments = listOf(
                navArgument("problemId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val problemId = backStackEntry.arguments?.getLong("problemId") ?: 0L
            ProblemDetailScreen(problemId = problemId)
        }

        // Revision Screen
        composable(Routes.Revision.route) {
            RevisionScreen(navController = navController)
        }

        // Stats Screen
        composable(Routes.Stats.route) {
            StatsScreen()
        }

        // Settings Screen
        composable(Routes.Settings.route) {
            SettingsScreen(navController = navController)
        }

        // About Screen
        composable(Routes.About.route) {
            AboutScreen()
        }
    }
}
