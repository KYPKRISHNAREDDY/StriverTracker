package com.dsatracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dsatracker.data.local.entity.ProblemStatus
import com.dsatracker.data.local.entity.RevisionRating
import com.dsatracker.ui.viewmodel.ProblemDetailViewModel
import com.dsatracker.utils.ChromeTabsHelper
import com.dsatracker.utils.rememberCustomTabsColor

@Composable
fun ProblemDetailScreen(
    problemId: Long,
    viewModel: ProblemDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val noteText by viewModel.noteText.collectAsState()
    val context = LocalContext.current
    val customTabsColor = rememberCustomTabsColor()

    ProblemDetailContent(
        uiState = uiState,
        noteText = noteText,
        onNoteTextChange = { viewModel.updateNoteText(it) },
        onStatusChange = { viewModel.updateStatus(it) },
        onToggleStarred = { viewModel.toggleStarred() },
        onScheduleRevision = { viewModel.scheduleRevision(it) },
        onOpenProblem = {
            uiState.problem?.let { problem ->
                ChromeTabsHelper.openUrl(context, problem.url, customTabsColor)
            }
        },
        onRefresh = { viewModel.refresh() }
    )
}

@Composable
private fun ProblemDetailContent(
    uiState: com.dsatracker.ui.viewmodel.ProblemDetailUiState,
    noteText: String,
    onNoteTextChange: (String) -> Unit,
    onStatusChange: (ProblemStatus) -> Unit,
    onToggleStarred: () -> Unit,
    onScheduleRevision: (RevisionRating) -> Unit,
    onOpenProblem: () -> Unit,
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
        uiState.problem == null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Problem not found")
            }
        }
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Problem Header
                ProblemHeader(
                    problem = uiState.problem,
                    isStarred = uiState.isStarred,
                    onToggleStarred = onToggleStarred
                )

                // Open Problem Button
                Button(
                    onClick = onOpenProblem,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Problem")
                }

                // Status Section
                StatusSection(
                    currentStatus = uiState.currentStatus,
                    onStatusChange = onStatusChange
                )

                // Solve Stats
                if (uiState.timesSolved > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Text("Solved ${uiState.timesSolved} time(s)")
                        }
                    }
                }

                // Revision Section
                if (uiState.canScheduleRevision) {
                    RevisionSection(onScheduleRevision = onScheduleRevision)
                }

                // Notes Section
                NotesSection(
                    noteText = noteText,
                    onNoteTextChange = onNoteTextChange
                )
            }
        }
    }
}

@Composable
private fun ProblemHeader(
    problem: com.dsatracker.data.local.entity.ProblemEntity,
    isStarred: Boolean,
    onToggleStarred: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                    text = problem.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onToggleStarred) {
                    Icon(
                        imageVector = if (isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Star",
                        tint = if (isStarred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Difficulty Badge
                val difficultyColor = when (problem.difficulty) {
                    com.dsatracker.data.local.entity.ProblemDifficulty.EASY -> MaterialTheme.colorScheme.tertiary
                    com.dsatracker.data.local.entity.ProblemDifficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
                    com.dsatracker.data.local.entity.ProblemDifficulty.HARD -> MaterialTheme.colorScheme.error
                }

                AssistChip(
                    onClick = {},
                    label = { Text(problem.difficulty.name) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = difficultyColor.copy(alpha = 0.2f),
                        labelColor = difficultyColor
                    )
                )

                // Platform Badge
                AssistChip(
                    onClick = {},
                    label = { Text(problem.platform.name) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusSection(
    currentStatus: ProblemStatus,
    onStatusChange: (ProblemStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProblemStatus.entries.forEach { status ->
                    FilterChip(
                        selected = currentStatus == status,
                        onClick = { onStatusChange(status) },
                        label = {
                            Text(
                                text = status.name.replace("_", " "),
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = if (currentStatus == status) {
                            { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
private fun RevisionSection(
    onScheduleRevision: (RevisionRating) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Revision",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Schedule this problem for spaced repetition review",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add to Revision")
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("How difficult was this problem?") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Choose difficulty to schedule next review:")
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            onScheduleRevision(RevisionRating.HARD)
                            showDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Hard (Review in 1 day)")
                    }

                    OutlinedButton(
                        onClick = {
                            onScheduleRevision(RevisionRating.OKAY)
                            showDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Okay (Review in 3 days)")
                    }

                    OutlinedButton(
                        onClick = {
                            onScheduleRevision(RevisionRating.EASY)
                            showDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Easy (Review in 7 days)")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun NotesSection(
    noteText: String,
    onNoteTextChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
                    text = "Notes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Auto-saved",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = noteText,
                onValueChange = onNoteTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = { Text("Write your approach, edge cases, learnings...") },
                maxLines = 10
            )
        }
    }
}
