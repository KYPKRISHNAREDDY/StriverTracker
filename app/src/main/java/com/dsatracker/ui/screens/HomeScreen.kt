package com.dsatracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dsatracker.ui.navigation.Routes
import com.dsatracker.ui.viewmodel.HomeViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        uiState = uiState,
        onRefresh = { viewModel.refresh() },
        onNavigateToProblems = { navController.navigate(Routes.SheetOverview.createRoute("STRIVER_A2Z")) },
        onNavigateToRevision = { navController.navigate(Routes.Revision.route) }
    )
}

@Composable
private fun HomeScreenContent(
    uiState: com.dsatracker.ui.viewmodel.HomeUiState,
    onRefresh: () -> Unit,
    onNavigateToProblems: () -> Unit,
    onNavigateToRevision: () -> Unit
) {
    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        uiState.error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Error: ${uiState.error}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = onRefresh) {
                        Text("Retry")
                    }
                }
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Greeting Section
                item {
                    GreetingSection()
                }

                // Stats Cards
                item {
                    StatsCardsSection(uiState)
                }

                // Today's Goal Progress
                item {
                    GoalProgressSection(uiState)
                }

                // Quick Actions
                item {
                    QuickActionsSection(
                        onNavigateToProblems = onNavigateToProblems,
                        onNavigateToRevision = onNavigateToRevision,
                        dueRevisionCount = uiState.dueRevisionCount
                    )
                }

                // Disclaimer
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    DisclaimerCard()
                }
            }
        }
    }
}

@Composable
private fun GreetingSection() {
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd")

    Column {
        Text(
            text = "Welcome back!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = today.format(dateFormatter),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatsCardsSection(uiState: com.dsatracker.ui.viewmodel.HomeUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Total Solved Card
        StatsCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.CheckCircle,
            value = uiState.totalSolved.toString(),
            label = "Solved",
            color = MaterialTheme.colorScheme.primary
        )

        // Streak Card
        StatsCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.LocalFireDepartment,
            value = "${uiState.currentStreak}",
            label = "Day Streak",
            color = MaterialTheme.colorScheme.tertiary
        )

        // Today's Problems Card
        StatsCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Today,
            value = "${uiState.todayProblemsSolved}",
            label = "Today",
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun StatsCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GoalProgressSection(uiState: com.dsatracker.ui.viewmodel.HomeUiState) {
    if (uiState.currentGoal != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.isGoalMetToday)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Goal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (uiState.isGoalMetToday) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Goal achieved",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { uiState.goalProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                val goalText = when (uiState.currentGoal.mode) {
                    com.dsatracker.data.local.entity.GoalMode.PROBLEMS_PER_DAY -> {
                        val target = uiState.currentGoal.targetProblemsPerDay ?: 0
                        "${uiState.todayProblemsSolved} / $target problems"
                    }
                    com.dsatracker.data.local.entity.GoalMode.MINUTES_PER_DAY -> {
                        val target = uiState.currentGoal.targetMinutesPerDay ?: 0
                        "${uiState.todayMinutesStudied} / $target minutes"
                    }
                }

                Text(
                    text = goalText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onNavigateToProblems: () -> Unit,
    onNavigateToRevision: () -> Unit,
    dueRevisionCount: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedCard(
                onClick = onNavigateToProblems,
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Problems",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Browse Problems",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            OutlinedCard(
                onClick = onNavigateToRevision,
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BadgedBox(
                        badge = {
                            if (dueRevisionCount > 0) {
                                Badge {
                                    Text(text = dueRevisionCount.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Revisions",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Revise",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun DisclaimerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Unofficial app. Not affiliated with Striver or takeUforward.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
