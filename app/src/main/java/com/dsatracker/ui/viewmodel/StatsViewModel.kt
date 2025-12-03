package com.dsatracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsatracker.data.local.entity.DailyStatsEntity
import com.dsatracker.data.preferences.PreferencesManager
import com.dsatracker.data.repository.DSARepository
import com.dsatracker.domain.usecase.CalculateStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for Stats Screen
 * Displays charts and statistics about user progress
 */
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: DSARepository,
    private val preferencesManager: PreferencesManager,
    private val calculateStreakUseCase: CalculateStreakUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // Get current user ID
                val userId = preferencesManager.getCurrentUserId() ?: run {
                    val newUserId = repository.createUser("Guest", null)
                    preferencesManager.setCurrentUserId(newUserId)
                    newUserId
                }

                // Combine all stats data
                combine(
                    repository.getSolvedCountFlow(userId),
                    repository.getRecentStats(userId, limit = 30),
                    repository.getTotalXpFlow(userId)
                ) { solvedCount, recentStats, totalXp ->
                    Triple(solvedCount, recentStats, totalXp)
                }.collect { (solvedCount, recentStats, totalXp) ->
                    // Calculate streak
                    val streak = calculateStreakUseCase(userId)

                    // Calculate weekly stats (last 7 days)
                    val last7Days = recentStats.take(7)
                    val weeklyProblemsSolved = last7Days.sumOf { it.problemsSolvedCount }
                    val weeklyRevisionsCount = last7Days.sumOf { it.revisionsCount }
                    val weeklyMinutesStudied = last7Days.sumOf { it.minutesStudied }

                    // Calculate monthly stats (last 30 days)
                    val monthlyProblemsSolved = recentStats.sumOf { it.problemsSolvedCount }
                    val monthlyRevisionsCount = recentStats.sumOf { it.revisionsCount }
                    val monthlyMinutesStudied = recentStats.sumOf { it.minutesStudied }

                    // Calculate average daily problems
                    val activeDays = recentStats.count { it.problemsSolvedCount > 0 }
                    val avgProblemsPerDay = if (activeDays > 0) {
                        monthlyProblemsSolved.toFloat() / activeDays.toFloat()
                    } else {
                        0f
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userId = userId,
                            totalSolved = solvedCount,
                            currentStreak = streak,
                            totalXp = totalXp ?: 0,
                            recentStats = recentStats,
                            weeklyProblemsSolved = weeklyProblemsSolved,
                            weeklyRevisionsCount = weeklyRevisionsCount,
                            weeklyMinutesStudied = weeklyMinutesStudied,
                            monthlyProblemsSolved = monthlyProblemsSolved,
                            monthlyRevisionsCount = monthlyRevisionsCount,
                            monthlyMinutesStudied = monthlyMinutesStudied,
                            avgProblemsPerDay = avgProblemsPerDay,
                            activeDaysThisMonth = activeDays,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load stats"
                    )
                }
            }
        }
    }

    fun refresh() {
        loadStats()
    }
}

/**
 * UI State for Stats Screen
 */
data class StatsUiState(
    val isLoading: Boolean = true,
    val userId: Long? = null,
    val totalSolved: Int = 0,
    val currentStreak: Int = 0,
    val totalXp: Int = 0,
    val recentStats: List<DailyStatsEntity> = emptyList(),
    val weeklyProblemsSolved: Int = 0,
    val weeklyRevisionsCount: Int = 0,
    val weeklyMinutesStudied: Int = 0,
    val monthlyProblemsSolved: Int = 0,
    val monthlyRevisionsCount: Int = 0,
    val monthlyMinutesStudied: Int = 0,
    val avgProblemsPerDay: Float = 0f,
    val activeDaysThisMonth: Int = 0,
    val error: String? = null
) {
    val weeklyHoursStudied: Float
        get() = weeklyMinutesStudied / 60f

    val monthlyHoursStudied: Float
        get() = monthlyMinutesStudied / 60f

    val consistencyPercentage: Int
        get() {
            val totalDays = 30 // Last 30 days
            return if (totalDays > 0) {
                (activeDaysThisMonth * 100) / totalDays
            } else {
                0
            }
        }
}
