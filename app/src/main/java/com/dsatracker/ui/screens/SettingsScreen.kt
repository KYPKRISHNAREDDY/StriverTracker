package com.dsatracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.dsatracker.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Show snackbar for messages
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle export intent
    LaunchedEffect(Unit) {
        viewModel.exportIntent.collect { intent ->
            context.startActivity(intent)
        }
    }

    LaunchedEffect(uiState.message, uiState.error) {
        when {
            uiState.message != null -> {
                snackbarHostState.showSnackbar(uiState.message!!)
                viewModel.clearMessage()
            }
            uiState.error != null -> {
                snackbarHostState.showSnackbar(uiState.error!!)
                viewModel.clearMessage()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        SettingsScreenContent(
            uiState = uiState,
            onToggleDailyReminder = { viewModel.toggleDailyReminder(it) },
            onSetReminderHour = { viewModel.setDailyReminderHour(it) },
            onToggleWeeklySummary = { viewModel.toggleWeeklySummary(it) },
            onExportNotes = { viewModel.exportNotes() },
            onNavigateToAbout = { navController.navigate(Routes.About.route) },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun SettingsScreenContent(
    uiState: com.dsatracker.ui.viewmodel.SettingsUiState,
    onToggleDailyReminder: (Boolean) -> Unit,
    onSetReminderHour: (Int) -> Unit,
    onToggleWeeklySummary: (Boolean) -> Unit,
    onExportNotes: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Notifications Section
        item {
            SettingsSection(title = "Notifications") {
                // Daily Reminder Toggle
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Daily Reminder",
                    subtitle = "Get reminded to practice daily"
                ) {
                    Switch(
                        checked = uiState.isDailyReminderEnabled,
                        onCheckedChange = onToggleDailyReminder
                    )
                }

                if (uiState.isDailyReminderEnabled) {
                    // Reminder Time Picker
                    var showTimePicker by remember { mutableStateOf(false) }

                    SettingsItem(
                        icon = Icons.Default.Schedule,
                        title = "Reminder Time",
                        subtitle = formatHour(uiState.dailyReminderHour)
                    ) {
                        TextButton(onClick = { showTimePicker = true }) {
                            Text("Change")
                        }
                    }

                    if (showTimePicker) {
                        TimePickerDialog(
                            currentHour = uiState.dailyReminderHour,
                            onDismiss = { showTimePicker = false },
                            onConfirm = { hour ->
                                onSetReminderHour(hour)
                                showTimePicker = false
                            }
                        )
                    }
                }

                // Weekly Summary Toggle
                SettingsItem(
                    icon = Icons.Default.Email,
                    title = "Weekly Summary",
                    subtitle = "Get a weekly progress summary"
                ) {
                    Switch(
                        checked = uiState.isWeeklySummaryEnabled,
                        onCheckedChange = onToggleWeeklySummary
                    )
                }
            }
        }

        // Data Section
        item {
            SettingsSection(title = "Data") {
                SettingsItem(
                    icon = Icons.Default.FileDownload,
                    title = "Export Notes",
                    subtitle = "Share your notes as text file",
                    onClick = onExportNotes
                )
            }
        }

        // About Section
        item {
            SettingsSection(title = "About") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About App",
                    subtitle = "Version, credits, and disclaimer",
                    onClick = onNavigateToAbout
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickableWithoutRipple(onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = clickModifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TimePickerDialog(
    currentHour: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedHour by remember { mutableStateOf(currentHour) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Reminder Time") },
        text = {
            Column {
                Text("Choose hour (24-hour format):")
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { selectedHour = (selectedHour - 1 + 24) % 24 }
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }

                    Text(
                        text = formatHour(selectedHour),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { selectedHour = (selectedHour + 1) % 24 }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedHour) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatHour(hour: Int): String {
    val period = if (hour < 12) "AM" else "PM"
    val displayHour = when (hour) {
        0 -> 12
        in 1..12 -> hour
        else -> hour - 12
    }
    return "$displayHour:00 $period"
}

@Composable
private fun Modifier.clickableWithoutRipple(onClick: () -> Unit): Modifier {
    return this.then(
        Modifier.clickable(
            indication = null,
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            onClick = onClick
        )
    )
}
