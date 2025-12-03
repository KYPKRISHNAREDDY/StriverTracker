package com.dsatracker.ui.navigation

/**
 * Navigation routes for the app
 * Defines all screen destinations
 */
sealed class Routes(val route: String) {
    // Onboarding & Auth
    object Splash : Routes("splash")
    object Onboarding : Routes("onboarding")
    object Auth : Routes("auth")
    object SetupGoal : Routes("setup_goal")

    // Main Screens
    object Home : Routes("home")
    object SheetOverview : Routes("sheet_overview/{sheetId}") {
        fun createRoute(sheetId: String) = "sheet_overview/$sheetId"
    }
    object TopicProblems : Routes("topic_problems/{topicId}") {
        fun createRoute(topicId: Long) = "topic_problems/$topicId"
    }
    object ProblemDetail : Routes("problem_detail/{problemId}") {
        fun createRoute(problemId: Long) = "problem_detail/$problemId"
    }

    // Revision & Practice
    object Revision : Routes("revision")
    object Notes : Routes("notes")

    // Progress & Stats
    object Calendar : Routes("calendar")
    object Stats : Routes("stats")

    // Settings
    object Settings : Routes("settings")
    object About : Routes("about")

    // Bottom Navigation Routes
    companion object {
        val bottomNavRoutes = listOf(
            Home,
            SheetOverview,
            Revision,
            Stats,
            Settings
        )

        /**
         * Get bottom navigation label for route
         */
        fun getBottomNavLabel(route: String): String {
            return when {
                route.startsWith(Home.route) -> "Home"
                route.startsWith("sheet_overview") -> "Problems"
                route.startsWith(Revision.route) -> "Revision"
                route.startsWith(Stats.route) -> "Stats"
                route.startsWith(Settings.route) -> "Settings"
                else -> ""
            }
        }

        /**
         * Check if route should show bottom navigation
         */
        fun shouldShowBottomNav(route: String?): Boolean {
            if (route == null) return false
            return route.startsWith(Home.route) ||
                    route.startsWith("sheet_overview") ||
                    route.startsWith(Revision.route) ||
                    route.startsWith(Stats.route) ||
                    route.startsWith(Settings.route)
        }

        /**
         * Check if route is a top-level destination (no back button)
         */
        fun isTopLevelDestination(route: String?): Boolean {
            if (route == null) return false
            return route == Home.route ||
                    route.startsWith("sheet_overview") ||
                    route == Revision.route ||
                    route == Stats.route ||
                    route == Settings.route
        }
    }
}
