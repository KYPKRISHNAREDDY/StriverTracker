package com.dsatracker.ui.screens

import androidx.compose.foundation.clickable
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
import com.dsatracker.data.local.entity.ProblemDifficulty
import com.dsatracker.data.local.entity.RevisionRating
import com.dsatracker.ui.navigation.Routes
import com.dsatracker.ui.viewmodel.RevisionViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun RevisionScreen(
    navController: NavController,
    viewModel: RevisionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    RevisionScreenContent(
        uiState = uiState,
        onProblemClick = { problemId ->
            navController.navigate(Routes.ProblemDetail.createRoute(problemId))
        },
        onCompleteRevision = { cardId, rating ->
            viewModel.completeRevision(cardId, rating)
        },
        onRefresh = { viewModel.refresh() }
    )
}

@Composable
private fun RevisionScreenContent(
    uiState: com.dsatracker.ui.viewmodel.RevisionUiState,
    onProblemClick: (Long) -> Unit,
    onCompleteRevision: (Long, RevisionRating) -> Unit,
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
        !uiState.hasDueCards -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "All caught up!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "No revisions due today",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                item {
                    RevisionHeader(dueCount = uiState.dueCount)
                }

                // Revision Cards
                items(uiState.revisionCards) { cardWithProblem ->
                    RevisionCard(
                        cardWithProblem = cardWithProblem,
                        onProblemClick = { onProblemClick(cardWithProblem.problem.id) },
                        onCompleteRevision = { rating ->
                            onCompleteRevision(cardWithProblem.card.id, rating)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RevisionHeader(dueCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Today's Revisions",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$dueCount problem(s) due for review",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun RevisionCard(
    cardWithProblem: com.dsatracker.ui.viewmodel.RevisionCardWithProblem,
    onProblemClick: () -> Unit,
    onCompleteRevision: (RevisionRating) -> Unit
) {
    var showRatingDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Problem Title and Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onProblemClick),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = cardWithProblem.problem.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Difficulty Badge
                        val difficultyColor = when (cardWithProblem.problem.difficulty) {
                            ProblemDifficulty.EASY -> MaterialTheme.colorScheme.tertiary
                            ProblemDifficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
                            ProblemDifficulty.HARD -> MaterialTheme.colorScheme.error
                        }

                        AssistChip(
                            onClick = {},
                            label = { Text(cardWithProblem.problem.difficulty.name, style = MaterialTheme.typography.labelSmall) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = difficultyColor.copy(alpha = 0.2f),
                                labelColor = difficultyColor
                            ),
                            modifier = Modifier.height(24.dp)
                        )

                        AssistChip(
                            onClick = {},
                            label = { Text("Review #${cardWithProblem.card.reviewCount + 1}", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Last reviewed info
            if (cardWithProblem.card.lastReviewedAtUtc != null) {
                Spacer(modifier = Modifier.height(8.dp))

                val lastReviewedDate = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(cardWithProblem.card.lastReviewedAtUtc),
                    ZoneId.systemDefault()
                )
                val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

                Text(
                    text = "Last reviewed: ${lastReviewedDate.format(formatter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mark as Reviewed Button
            Button(
                onClick = { showRatingDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mark as Reviewed")
            }
        }
    }

    // Rating Dialog
    if (showRatingDialog) {
        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            title = { Text("How did the revision go?") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Rate your understanding:")

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            onCompleteRevision(RevisionRating.HARD)
                            showRatingDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Hard (Review again in 1 day)")
                    }

                    OutlinedButton(
                        onClick = {
                            onCompleteRevision(RevisionRating.OKAY)
                            showRatingDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Okay (Review again in 3 days)")
                    }

                    OutlinedButton(
                        onClick = {
                            onCompleteRevision(RevisionRating.EASY)
                            showRatingDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Easy (Review again in 7 days)")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showRatingDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
