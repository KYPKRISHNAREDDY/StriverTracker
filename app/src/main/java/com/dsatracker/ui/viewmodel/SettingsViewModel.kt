package com.dsatracker.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsatracker.data.preferences.PreferencesManager
import com.dsatracker.domain.usecase.ExportNotesUseCase
import com.dsatracker.utils.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Settings Screen
 * Manages app settings and preferences
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val exportNotesUseCase: ExportNotesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _exportIntent = MutableSharedFlow<Intent>()
    val exportIntent: SharedFlow<Intent> = _exportIntent.asSharedFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                preferencesManager.isDailyReminderEnabled,
                preferencesManager.isWeeklySummaryEnabled
            ) { dailyReminder, weeklySummary ->
                Pair(dailyReminder, weeklySummary)
            }.collect { (dailyReminder, weeklySummary) ->
                val reminderHour = preferencesManager.getDailyReminderHour()

                _uiState.update {
                    it.copy(
                        isDailyReminderEnabled = dailyReminder,
                        dailyReminderHour = reminderHour.toInt(),
                        isWeeklySummaryEnabled = weeklySummary
                    )
                }
            }
        }
    }

    fun toggleDailyReminder(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setDailyReminderEnabled(enabled)
            rescheduleNotifications()
        }
    }

    fun setDailyReminderHour(hour: Int) {
        viewModelScope.launch {
            preferencesManager.setDailyReminderHour(hour.toLong())
            rescheduleNotifications()
        }
    }

    fun toggleWeeklySummary(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setWeeklySummaryEnabled(enabled)
            rescheduleNotifications()
        }
    }

    private suspend fun rescheduleNotifications() {
        val isDailyEnabled = preferencesManager.isDailyReminderEnabled.first()
        val dailyHour = preferencesManager.getDailyReminderHour().toInt()
        val isWeeklyEnabled = preferencesManager.isWeeklySummaryEnabled.first()
        NotificationScheduler.rescheduleAll(context, isDailyEnabled, dailyHour, isWeeklyEnabled)
    }

    fun exportNotes() {
        viewModelScope.launch {
            try {
                val result = exportNotesUseCase()
                if (result.isSuccess) {
                    _exportIntent.emit(result.getOrThrow())
                    _uiState.update {
                        it.copy(message = "Notes exported successfully")
                    }
                } else {
                    _uiState.update {
                        it.copy(error = result.exceptionOrNull()?.message ?: "Failed to export notes")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to export notes: ${e.message}")
                }
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                preferencesManager.clearAll()
                _uiState.update {
                    it.copy(message = "All data cleared successfully")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to clear data: ${e.message}")
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}

/**
 * UI State for Settings Screen
 */
data class SettingsUiState(
    val isDailyReminderEnabled: Boolean = true,
    val dailyReminderHour: Int = 9, // 9 AM default
    val isWeeklySummaryEnabled: Boolean = true,
    val message: String? = null,
    val error: String? = null
)
