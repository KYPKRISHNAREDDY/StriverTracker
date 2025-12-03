package com.dsatracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dsatracker.data.local.dao.TopicWithProgress
import com.dsatracker.ui.navigation.Routes
import com.dsatracker.ui.viewmodel.SheetViewModel

@Composable
fun SheetOverviewScreen(
    sheetId: String,
    navController: NavController,
    viewModel: SheetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    SheetOverviewContent(
        uiState = uiState,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        onTopicClick = { topicId ->
            navController.navigate(Routes.TopicProblems.createRoute(topicId))
        },
        onRefresh = { viewModel.refresh() }
    )
}

@Composable
private fun SheetOverviewContent(
    uiState: com.dsatracker.ui.viewmodel.SheetUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onTopicClick: (Long) -> Unit,
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
                // Header Section
                SheetHeader(uiState)

                // Search Bar
                SearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange
                )

                // Topics List
                val filteredTopics = if (searchQuery.isBlank()) {
                    uiState.topics
                } else {
                    uiState.topics.filter {
                        it.name.contains(searchQuery, ignoreCase = true)
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredTopics) { topic ->
                        TopicCard(
                            topic = topic,
                            onClick = { onTopicClick(topic.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetHeader(uiState: com.dsatracker.ui.viewmodel.SheetUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = uiState.sheet?.name ?: "DSA Sheet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${uiState.solvedProblems} / ${uiState.totalProblems} problems solved",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { uiState.overallProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${uiState.overallProgressPercentage}% complete",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        placeholder = { Text("Search topics...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        singleLine = true
    )

    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun TopicCard(
    topic: TopicWithProgress,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    text = topic.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${topic.solvedProblems}/${topic.totalProblems}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = {
                    if (topic.totalProblems > 0)
                        topic.solvedProblems.toFloat() / topic.totalProblems.toFloat()
                    else 0f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
            )

            Spacer(modifier = Modifier.height(4.dp))

            val percentage = if (topic.totalProblems > 0) {
                (topic.solvedProblems * 100) / topic.totalProblems
            } else 0

            Text(
                text = "$percentage% complete",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
