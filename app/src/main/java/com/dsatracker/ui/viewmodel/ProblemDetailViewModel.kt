package com.dsatracker.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsatracker.data.local.entity.*
import com.dsatracker.data.preferences.PreferencesManager
import com.dsatracker.data.repository.DSARepository
import com.dsatracker.domain.usecase.ScheduleRevisionCardUseCase
import com.dsatracker.domain.usecase.UpdateProblemStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Problem Detail Screen
 * Handles problem status, notes, and revision scheduling
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class ProblemDetailViewModel @Inject constructor(
    private val repository: DSARepository,
    private val preferencesManager: PreferencesManager,
    private val updateProblemStatusUseCase: UpdateProblemStatusUseCase,
    private val scheduleRevisionCardUseCase: ScheduleRevisionCardUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val problemId: Long = savedStateHandle.get<Long>("problemId") ?: 0L

    private val _uiState = MutableStateFlow(ProblemDetailUiState())
    val uiState: StateFlow<ProblemDetailUiState> = _uiState.asStateFlow()

    private val _noteText = MutableStateFlow("")
    val noteText: StateFlow<String> = _noteText.asStateFlow()

    init {
        loadProblemData()
        setupNoteAutosave()
    }

    private fun loadProblemData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // Get current user ID
                val userId = preferencesManager.getCurrentUserId() ?: run {
                    val newUserId = repository.createUser("Guest", null)
                    preferencesManager.setCurrentUserId(newUserId)
                    newUserId
                }

                // Get problem details
                val problem = repository.getProblem(problemId)

                if (problem == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Problem not found"
                        )
                    }
                    return@launch
                }

                // Combine problem data streams
                combine(
                    repository.getProgressFlow(userId, problemId),
                    repository.getNoteFlow(userId, problemId),
                    repository.getDueRevisionCountFlow(userId, System.currentTimeMillis())
                ) { progress, note, _ ->
                    Triple(progress, note, Unit)
                }.collect { (progress, note) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userId = userId,
                            problem = problem,
                            progress = progress,
                            error = null
                        )
                    }

                    // Update note text if different
                    val noteContent = note?.content ?: ""
                    if (_noteText.value != noteContent) {
                        _noteText.value = noteContent
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load problem"
                    )
                }
            }
        }
    }

    private fun setupNoteAutosave() {
        viewModelScope.launch {
            _noteText
                .debounce(1000) // Wait 1 second after user stops typing
                .distinctUntilChanged()
                .drop(1) // Skip initial value
                .collect { text ->
                    val userId = _uiState.value.userId ?: return@collect
                    if (text.isNotBlank()) {
                        repository.saveNote(userId, problemId, text)
                    }
                }
        }
    }

    fun updateNoteText(text: String) {
        _noteText.value = text
    }

    fun updateStatus(newStatus: ProblemStatus) {
        viewModelScope.launch {
            try {
                val userId = _uiState.value.userId ?: return@launch

                updateProblemStatusUseCase(
                    userId = userId,
                    problemId = problemId,
                    newStatus = newStatus
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to update status: ${e.message}")
                }
            }
        }
    }

    fun scheduleRevision(rating: RevisionRating) {
        viewModelScope.launch {
            try {
                val userId = _uiState.value.userId ?: return@launch

                scheduleRevisionCardUseCase(
                    userId = userId,
                    problemId = problemId,
                    rating = rating
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to schedule revision: ${e.message}")
                }
            }
        }
    }

    fun toggleStarred() {
        viewModelScope.launch {
            try {
                val progress = _uiState.value.progress ?: return@launch
                val updatedProgress = progress.copy(starred = !progress.starred)
                repository.updateProgress(updatedProgress)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to toggle starred: ${e.message}")
                }
            }
        }
    }

    fun refresh() {
        loadProblemData()
    }
}

/**
 * UI State for Problem Detail Screen
 */
data class ProblemDetailUiState(
    val isLoading: Boolean = true,
    val userId: Long? = null,
    val problem: ProblemEntity? = null,
    val progress: UserProblemProgressEntity? = null,
    val error: String? = null
) {
    val currentStatus: ProblemStatus
        get() = progress?.status ?: ProblemStatus.NOT_STARTED

    val isStarred: Boolean
        get() = progress?.starred ?: false

    val timesSolved: Int
        get() = progress?.timesSolved ?: 0

    val lastSolvedAt: Long?
        get() = progress?.lastSolvedAt

    val canScheduleRevision: Boolean
        get() = currentStatus == ProblemStatus.SOLVED
}
