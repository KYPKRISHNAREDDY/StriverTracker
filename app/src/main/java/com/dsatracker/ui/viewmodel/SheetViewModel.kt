package com.dsatracker.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsatracker.data.local.dao.TopicWithProgress
import com.dsatracker.data.local.entity.SheetEntity
import com.dsatracker.data.preferences.PreferencesManager
import com.dsatracker.data.repository.DSARepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Sheet Overview Screen
 * Displays all topics with progress for a given sheet
 */
@HiltViewModel
class SheetViewModel @Inject constructor(
    private val repository: DSARepository,
    private val preferencesManager: PreferencesManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sheetId: String = savedStateHandle.get<String>("sheetId") ?: "STRIVER_A2Z"

    private val _uiState = MutableStateFlow(SheetUiState())
    val uiState: StateFlow<SheetUiState> = _uiState.asStateFlow()

    init {
        loadSheetData()
    }

    private fun loadSheetData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // Get current user ID
                val userId = preferencesManager.getCurrentUserId() ?: run {
                    val newUserId = repository.createUser("Guest", null)
                    preferencesManager.setCurrentUserId(newUserId)
                    newUserId
                }

                // Get sheet details
                val sheet = repository.getSheet(sheetId)

                if (sheet == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Sheet not found. Please ensure data is loaded."
                        )
                    }
                    return@launch
                }

                // Collect topics with progress
                repository.getTopicsWithProgress(sheetId, userId)
                    .collect { topics ->
                        val totalProblems = topics.sumOf { it.totalProblems }
                        val solvedProblems = topics.sumOf { it.solvedProblems }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                sheet = sheet,
                                topics = topics,
                                totalProblems = totalProblems,
                                solvedProblems = solvedProblems,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load sheet data"
                    )
                }
            }
        }
    }

    fun refresh() {
        loadSheetData()
    }
}

/**
 * UI State for Sheet Overview Screen
 */
data class SheetUiState(
    val isLoading: Boolean = true,
    val sheet: SheetEntity? = null,
    val topics: List<TopicWithProgress> = emptyList(),
    val totalProblems: Int = 0,
    val solvedProblems: Int = 0,
    val error: String? = null
) {
    val overallProgress: Float
        get() = if (totalProblems > 0) {
            solvedProblems.toFloat() / totalProblems.toFloat()
        } else {
            0f
        }

    val overallProgressPercentage: Int
        get() = (overallProgress * 100).toInt()
}
