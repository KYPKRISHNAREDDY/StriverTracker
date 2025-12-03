package com.dsatracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dsatracker.ui.navigation.DSANavGraph
import com.dsatracker.ui.navigation.Routes
import com.dsatracker.ui.theme.DSATrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DSATrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DSATrackerApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DSATrackerApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = getScreenTitle(currentRoute),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    if (currentRoute != null && !Routes.isTopLevelDestination(currentRoute)) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (Routes.shouldShowBottomNav(currentRoute)) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == Routes.Home.route,
                        onClick = {
                            navController.navigate(Routes.Home.route) {
                                popUpTo(Routes.Home.route) { inclusive = true }
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.List, contentDescription = "Problems") },
                        label = { Text("Problems") },
                        selected = currentRoute?.startsWith("sheet_overview") == true,
                        onClick = {
                            navController.navigate(Routes.SheetOverview.createRoute("STRIVER_A2Z")) {
                                popUpTo(Routes.Home.route)
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Refresh, contentDescription = "Revision") },
                        label = { Text("Revision") },
                        selected = currentRoute == Routes.Revision.route,
                        onClick = {
                            navController.navigate(Routes.Revision.route) {
                                popUpTo(Routes.Home.route)
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Analytics, contentDescription = "Stats") },
                        label = { Text("Stats") },
                        selected = currentRoute == Routes.Stats.route,
                        onClick = {
                            navController.navigate(Routes.Stats.route) {
                                popUpTo(Routes.Home.route)
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = currentRoute == Routes.Settings.route,
                        onClick = {
                            navController.navigate(Routes.Settings.route) {
                                popUpTo(Routes.Home.route)
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        DSANavGraph(
            navController = navController,
            startDestination = Routes.Home.route,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

private fun getScreenTitle(route: String?): String {
    return when {
        route == null -> "DSA Tracker"
        route == Routes.Home.route -> "Home"
        route.startsWith("sheet_overview") -> "Problems"
        route.startsWith("topic_problems") -> "Topic"
        route.startsWith("problem_detail") -> "Problem"
        route == Routes.Revision.route -> "Revision"
        route == Routes.Notes.route -> "Notes"
        route == Routes.Calendar.route -> "Calendar"
        route == Routes.Stats.route -> "Statistics"
        route == Routes.Settings.route -> "Settings"
        route == Routes.About.route -> "About"
        else -> "DSA Tracker"
    }
}
