package com.dsatracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.dsatracker.data.local.dao.ProblemWithStatus
import com.dsatracker.data.local.entity.ProblemDifficulty
import com.dsatracker.data.local.entity.ProblemStatus
import com.dsatracker.ui.navigation.Routes
import com.dsatracker.ui.viewmodel.TopicViewModel

@Composable
fun TopicProblemsScreen(
    topicId: Long,
    navController: NavController,
    viewModel: TopicViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()

    TopicProblemsContent(
        uiState = uiState,
        filterState = filterState,
        onProblemClick = { problemId ->
            navController.navigate(Routes.ProblemDetail.createRoute(problemId))
        },
        onStatusFilterChange = { viewModel.setStatusFilter(it) },
        onDifficultyFilterChange = { viewModel.setDifficultyFilter(it) },
        onStarredToggle = { viewModel.setShowStarredOnly(!filterState.showStarredOnly) },
        onNeedsRevisionToggle = { viewModel.setShowNeedsRevisionOnly(!filterState.showNeedsRevisionOnly) },
        onSearchQueryChange = { viewModel.setSearchQuery(it) },
        onClearFilters = { viewModel.clearFilters() },
        onRefresh = { viewModel.refresh() }
    )
}

@Composable
private fun TopicProblemsContent(
    uiState: com.dsatracker.ui.viewmodel.TopicUiState,
    filterState: com.dsatracker.ui.viewmodel.FilterState,
    onProblemClick: (Long) -> Unit,
    onStatusFilterChange: (ProblemStatus?) -> Unit,
    onDifficultyFilterChange: (ProblemDifficulty?) -> Unit,
    onStarredToggle: () -> Unit,
    onNeedsRevisionToggle: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearFilters: () -> Unit,
    onRefresh: () -> Unit
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Topic Header
                TopicHeader(uiState)

                // Search Bar
                OutlinedTextField(
                    value = filterState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search problems...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    singleLine = true
                )

                // Filters Section
                FiltersSection(
                    filterState = filterState,
                    onStatusFilterChange = onStatusFilterChange,
                    onDifficultyFilterChange = onDifficultyFilterChange,
                    onStarredToggle = onStarredToggle,
                    onNeedsRevisionToggle = onNeedsRevisionToggle,
                    onClearFilters = onClearFilters
                )

                // Problems List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.problems) { problem ->
                        ProblemCard(
                            problem = problem,
                            onClick = { onProblemClick(problem.id) }
                        )
                    }

                    if (uiState.problems.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No problems match your filters",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicHeader(uiState: com.dsatracker.ui.viewmodel.TopicUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = uiState.topic?.name ?: "Topic",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${uiState.solvedCount} / ${uiState.totalCount} solved (${uiState.progressPercentage}%)",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { uiState.progressPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            )
        }
    }
}

@Composable
private fun FiltersSection(
    filterState: com.dsatracker.ui.viewmodel.FilterState,
    onStatusFilterChange: (ProblemStatus?) -> Unit,
    onDifficultyFilterChange: (ProblemDifficulty?) -> Unit,
    onStarredToggle: () -> Unit,
    onNeedsRevisionToggle: () -> Unit,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Status Filters
        Text(
            text = "Status",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            item {
                FilterChip(
                    selected = filterState.statusFilter == null,
                    onClick = { onStatusFilterChange(null) },
                    label = { Text("All") }
                )
            }
            items(ProblemStatus.entries.toTypedArray()) { status ->
                FilterChip(
                    selected = filterState.statusFilter == status,
                    onClick = { onStatusFilterChange(status) },
                    label = { Text(status.name.replace("_", " ")) }
                )
            }
        }

        // Difficulty Filters
        Text(
            text = "Difficulty",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            item {
                FilterChip(
                    selected = filterState.difficultyFilter == null,
                    onClick = { onDifficultyFilterChange(null) },
                    label = { Text("All") }
                )
            }
            items(ProblemDifficulty.entries.toTypedArray()) { difficulty ->
                FilterChip(
                    selected = filterState.difficultyFilter == difficulty,
                    onClick = { onDifficultyFilterChange(difficulty) },
                    label = { Text(difficulty.name) }
                )
            }
        }

        // Toggle Filters
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterState.showStarredOnly,
                onClick = onStarredToggle,
                label = { Text("Starred") },
                leadingIcon = if (filterState.showStarredOnly) {
                    { Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null
            )

            FilterChip(
                selected = filterState.showNeedsRevisionOnly,
                onClick = onNeedsRevisionToggle,
                label = { Text("Needs Revision") }
            )
        }

        // Clear Filters Button
        if (filterState.hasActiveFilters) {
            TextButton(onClick = onClearFilters) {
                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear Filters")
            }
        }
    }
}

@Composable
private fun ProblemCard(
    problem: ProblemWithStatus,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = problem.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    if (problem.starred == true) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Starred",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (problem.needsRevision == true) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Needs Revision",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Difficulty Badge
                    DifficultyBadge(problem.difficulty)

                    // Platform Badge
                    AssistChip(
                        onClick = {},
                        label = { Text(problem.platform.name, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }

            // Status Badge
            StatusBadge(problem.status)
        }
    }
}

@Composable
private fun DifficultyBadge(difficulty: ProblemDifficulty) {
    val color = when (difficulty) {
        ProblemDifficulty.EASY -> MaterialTheme.colorScheme.tertiary
        ProblemDifficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
        ProblemDifficulty.HARD -> MaterialTheme.colorScheme.error
    }

    AssistChip(
        onClick = {},
        label = {
            Text(
                text = difficulty.name,
                style = MaterialTheme.typography.labelSmall
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.2f),
            labelColor = color
        ),
        modifier = Modifier.height(24.dp)
    )
}

@Composable
private fun StatusBadge(status: ProblemStatus?) {
    val (text, color) = when (status) {
        ProblemStatus.SOLVED -> "✓" to MaterialTheme.colorScheme.primary
        ProblemStatus.IN_PROGRESS -> "..." to MaterialTheme.colorScheme.secondary
        ProblemStatus.SKIPPED -> "⊘" to MaterialTheme.colorScheme.error
        else -> "○" to MaterialTheme.colorScheme.outline
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
