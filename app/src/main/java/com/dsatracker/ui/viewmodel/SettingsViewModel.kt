package com.dsatracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsatracker.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Settings Screen
 * Manages app settings and preferences
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

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
        }
    }

    fun setDailyReminderHour(hour: Int) {
        viewModelScope.launch {
            preferencesManager.setDailyReminderHour(hour.toLong())
        }
    }

    fun toggleWeeklySummary(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setWeeklySummaryEnabled(enabled)
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
