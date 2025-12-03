package com.dsatracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsatracker.data.local.entity.GoalEntity
import com.dsatracker.data.local.entity.RevisionCardEntity
import com.dsatracker.data.preferences.PreferencesManager
import com.dsatracker.data.repository.DSARepository
import com.dsatracker.domain.usecase.CalculateStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * ViewModel for the Home screen
 * Displays today's dashboard with streak, goals, and due items
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DSARepository,
    private val preferencesManager: PreferencesManager,
    private val calculateStreakUseCase: CalculateStreakUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // Get current user ID
                val userId = preferencesManager.getCurrentUserId() ?: run {
                    // Create default guest user if none exists
                    val newUserId = repository.createUser("Guest", null)
                    preferencesManager.setCurrentUserId(newUserId)
                    newUserId
                }

                // Collect all data streams
                combine(
                    repository.getSolvedCountFlow(userId),
                    getDueRevisionCards(userId),
                    getCurrentGoal(userId),
                    getTodayStats(userId)
                ) { solvedCount, dueRevisions, currentGoal, todayStats ->
                    HomeData(
                        solvedCount = solvedCount,
                        dueRevisions = dueRevisions,
                        currentGoal = currentGoal,
                        todayProblemsSolved = todayStats?.problemsSolvedCount ?: 0,
                        todayRevisionsCount = todayStats?.revisionsCount ?: 0,
                        todayMinutesStudied = todayStats?.minutesStudied ?: 0
                    )
                }.collect { homeData ->
                    // Calculate streak
                    val streak = calculateStreakUseCase(userId)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userId = userId,
                            totalSolved = homeData.solvedCount,
                            dueRevisionCount = homeData.dueRevisions.size,
                            currentStreak = streak,
                            currentGoal = homeData.currentGoal,
                            todayProblemsSolved = homeData.todayProblemsSolved,
                            todayRevisionsCount = homeData.todayRevisionsCount,
                            todayMinutesStudied = homeData.todayMinutesStudied,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load home data"
                    )
                }
            }
        }
    }

    private fun getDueRevisionCards(userId: Long): Flow<List<RevisionCardEntity>> {
        val endDate = System.currentTimeMillis()
        return repository.getDueRevisionCards(userId, endDate, limit = 15)
    }

    private fun getCurrentGoal(userId: Long): Flow<GoalEntity?> {
        return repository.getActiveGoalFlow(userId)
    }

    private fun getTodayStats(userId: Long): Flow<com.dsatracker.data.local.entity.DailyStatsEntity?> {
        val today = LocalDate.now().toEpochDay()
        return repository.getStatsFlow(userId, today)
    }

    fun refresh() {
        loadHomeData()
    }

    private data class HomeData(
        val solvedCount: Int,
        val dueRevisions: List<RevisionCardEntity>,
        val currentGoal: GoalEntity?,
        val todayProblemsSolved: Int,
        val todayRevisionsCount: Int,
        val todayMinutesStudied: Int
    )
}

/**
 * UI State for Home screen
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val userId: Long? = null,
    val totalSolved: Int = 0,
    val dueRevisionCount: Int = 0,
    val currentStreak: Int = 0,
    val currentGoal: GoalEntity? = null,
    val todayProblemsSolved: Int = 0,
    val todayRevisionsCount: Int = 0,
    val todayMinutesStudied: Int = 0,
    val error: String? = null
) {
    val isGoalMetToday: Boolean
        get() = currentGoal?.let { goal ->
            when (goal.mode) {
                com.dsatracker.data.local.entity.GoalMode.PROBLEMS_PER_DAY -> {
                    val target = goal.targetProblemsPerDay ?: 0
                    todayProblemsSolved >= target
                }
                com.dsatracker.data.local.entity.GoalMode.MINUTES_PER_DAY -> {
                    val target = goal.targetMinutesPerDay ?: 0
                    todayMinutesStudied >= target
                }
            }
        } ?: false

    val goalProgress: Float
        get() = currentGoal?.let { goal ->
            when (goal.mode) {
                com.dsatracker.data.local.entity.GoalMode.PROBLEMS_PER_DAY -> {
                    val target = goal.targetProblemsPerDay?.toFloat() ?: 1f
                    (todayProblemsSolved.toFloat() / target).coerceIn(0f, 1f)
                }
                com.dsatracker.data.local.entity.GoalMode.MINUTES_PER_DAY -> {
                    val target = goal.targetMinutesPerDay?.toFloat() ?: 1f
                    (todayMinutesStudied.toFloat() / target).coerceIn(0f, 1f)
                }
            }
        } ?: 0f
}
