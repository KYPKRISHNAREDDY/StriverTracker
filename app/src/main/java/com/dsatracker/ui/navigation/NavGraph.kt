package com.dsatracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

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
        // Splash & Onboarding
        composable(Routes.Splash.route) {
            // SplashScreen(navController = navController)
            PlaceholderScreen(title = "Splash Screen")
        }

        composable(Routes.Onboarding.route) {
            // OnboardingScreen(navController = navController)
            PlaceholderScreen(title = "Onboarding Screen")
        }

        composable(Routes.Auth.route) {
            // AuthScreen(navController = navController)
            PlaceholderScreen(title = "Auth Screen")
        }

        composable(Routes.SetupGoal.route) {
            // SetupGoalScreen(navController = navController)
            PlaceholderScreen(title = "Setup Goal Screen")
        }

        // Home Screen
        composable(Routes.Home.route) {
            // HomeScreen(navController = navController)
            PlaceholderScreen(title = "Home Screen")
        }

        // Sheet Overview with sheetId parameter
        composable(
            route = Routes.SheetOverview.route,
            arguments = listOf(
                navArgument("sheetId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sheetId = backStackEntry.arguments?.getString("sheetId") ?: ""
            // SheetOverviewScreen(
            //     sheetId = sheetId,
            //     navController = navController
            // )
            PlaceholderScreen(title = "Sheet Overview: $sheetId")
        }

        // Topic Problems with topicId parameter
        composable(
            route = Routes.TopicProblems.route,
            arguments = listOf(
                navArgument("topicId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getLong("topicId") ?: 0L
            // TopicProblemsScreen(
            //     topicId = topicId,
            //     navController = navController
            // )
            PlaceholderScreen(title = "Topic Problems: $topicId")
        }

        // Problem Detail with problemId parameter
        composable(
            route = Routes.ProblemDetail.route,
            arguments = listOf(
                navArgument("problemId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val problemId = backStackEntry.arguments?.getLong("problemId") ?: 0L
            // ProblemDetailScreen(
            //     problemId = problemId,
            //     navController = navController
            // )
            PlaceholderScreen(title = "Problem Detail: $problemId")
        }

        // Revision Screen
        composable(Routes.Revision.route) {
            // RevisionScreen(navController = navController)
            PlaceholderScreen(title = "Revision Screen")
        }

        // Notes Screen
        composable(Routes.Notes.route) {
            // NotesScreen(navController = navController)
            PlaceholderScreen(title = "Notes Screen")
        }

        // Calendar Screen
        composable(Routes.Calendar.route) {
            // CalendarScreen(navController = navController)
            PlaceholderScreen(title = "Calendar Screen")
        }

        // Stats Screen
        composable(Routes.Stats.route) {
            // StatsScreen(navController = navController)
            PlaceholderScreen(title = "Stats Screen")
        }

        // Settings Screen
        composable(Routes.Settings.route) {
            // SettingsScreen(navController = navController)
            PlaceholderScreen(title = "Settings Screen")
        }

        // About Screen
        composable(Routes.About.route) {
            // AboutScreen(navController = navController)
            PlaceholderScreen(title = "About Screen")
        }
    }
}

/**
 * Placeholder screen for development
 * Replace with actual screens as they are implemented
 */
@Composable
private fun PlaceholderScreen(title: String) {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = title,
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )
    }
}
