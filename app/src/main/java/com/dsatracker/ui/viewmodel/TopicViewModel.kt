package com.dsatracker.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsatracker.data.local.dao.ProblemWithStatus
import com.dsatracker.data.local.entity.ProblemDifficulty
import com.dsatracker.data.local.entity.ProblemStatus
import com.dsatracker.data.local.entity.TopicEntity
import com.dsatracker.data.preferences.PreferencesManager
import com.dsatracker.data.repository.DSARepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Topic Problems Screen
 * Displays all problems within a topic with filtering options
 */
@HiltViewModel
class TopicViewModel @Inject constructor(
    private val repository: DSARepository,
    private val preferencesManager: PreferencesManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val topicId: Long = savedStateHandle.get<Long>("topicId") ?: 0L

    private val _uiState = MutableStateFlow(TopicUiState())
    val uiState: StateFlow<TopicUiState> = _uiState.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    init {
        loadTopicData()
    }

    private fun loadTopicData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // Get current user ID
                val userId = preferencesManager.getCurrentUserId() ?: run {
                    val newUserId = repository.createUser("Guest", null)
                    preferencesManager.setCurrentUserId(newUserId)
                    newUserId
                }

                // Get topic details
                val topic = repository.getTopic(topicId)

                if (topic == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Topic not found"
                        )
                    }
                    return@launch
                }

                // Collect problems with status and apply filters
                combine(
                    repository.getProblemsWithStatus(topicId, userId),
                    _filterState
                ) { problems, filter ->
                    applyFilters(problems, filter)
                }.collect { filteredProblems ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            topic = topic,
                            problems = filteredProblems,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load problems"
                    )
                }
            }
        }
    }

    private fun applyFilters(
        problems: List<ProblemWithStatus>,
        filter: FilterState
    ): List<ProblemWithStatus> {
        var filtered = problems

        // Filter by status
        if (filter.statusFilter != null) {
            filtered = filtered.filter { it.status == filter.statusFilter }
        }

        // Filter by difficulty
        if (filter.difficultyFilter != null) {
            filtered = filtered.filter { it.difficulty == filter.difficultyFilter }
        }

        // Filter by starred
        if (filter.showStarredOnly) {
            filtered = filtered.filter { it.starred == true }
        }

        // Filter by needs revision
        if (filter.showNeedsRevisionOnly) {
            filtered = filtered.filter { it.needsRevision == true }
        }

        // Filter by search query
        if (filter.searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(filter.searchQuery, ignoreCase = true)
            }
        }

        return filtered
    }

    fun setStatusFilter(status: ProblemStatus?) {
        _filterState.update { it.copy(statusFilter = status) }
    }

    fun setDifficultyFilter(difficulty: ProblemDifficulty?) {
        _filterState.update { it.copy(difficultyFilter = difficulty) }
    }

    fun setShowStarredOnly(show: Boolean) {
        _filterState.update { it.copy(showStarredOnly = show) }
    }

    fun setShowNeedsRevisionOnly(show: Boolean) {
        _filterState.update { it.copy(showNeedsRevisionOnly = show) }
    }

    fun setSearchQuery(query: String) {
        _filterState.update { it.copy(searchQuery = query) }
    }

    fun clearFilters() {
        _filterState.update { FilterState() }
    }

    fun refresh() {
        loadTopicData()
    }
}

/**
 * UI State for Topic Problems Screen
 */
data class TopicUiState(
    val isLoading: Boolean = true,
    val topic: TopicEntity? = null,
    val problems: List<ProblemWithStatus> = emptyList(),
    val error: String? = null
) {
    val solvedCount: Int
        get() = problems.count { it.status == ProblemStatus.SOLVED }

    val totalCount: Int
        get() = problems.size

    val progressPercentage: Int
        get() = if (totalCount > 0) {
            (solvedCount * 100) / totalCount
        } else {
            0
        }
}

/**
 * Filter state for problems
 */
data class FilterState(
    val statusFilter: ProblemStatus? = null,
    val difficultyFilter: ProblemDifficulty? = null,
    val showStarredOnly: Boolean = false,
    val showNeedsRevisionOnly: Boolean = false,
    val searchQuery: String = ""
) {
    val hasActiveFilters: Boolean
        get() = statusFilter != null ||
                difficultyFilter != null ||
                showStarredOnly ||
                showNeedsRevisionOnly ||
                searchQuery.isNotBlank()
}
