package com.dsatracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsatracker.data.local.entity.ProblemEntity
import com.dsatracker.data.local.entity.RevisionCardEntity
import com.dsatracker.data.local.entity.RevisionRating
import com.dsatracker.data.preferences.PreferencesManager
import com.dsatracker.data.repository.DSARepository
import com.dsatracker.domain.usecase.ScheduleRevisionCardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Revision Screen
 * Displays due revision cards and handles revision completion
 */
@HiltViewModel
class RevisionViewModel @Inject constructor(
    private val repository: DSARepository,
    private val preferencesManager: PreferencesManager,
    private val scheduleRevisionCardUseCase: ScheduleRevisionCardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RevisionUiState())
    val uiState: StateFlow<RevisionUiState> = _uiState.asStateFlow()

    init {
        loadRevisionCards()
    }

    private fun loadRevisionCards() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // Get current user ID
                val userId = preferencesManager.getCurrentUserId() ?: run {
                    val newUserId = repository.createUser("Guest", null)
                    preferencesManager.setCurrentUserId(newUserId)
                    newUserId
                }

                // Get due revision cards
                val endDate = System.currentTimeMillis()
                repository.getDueRevisionCards(userId, endDate, limit = 15)
                    .collect { cards ->
                        // Get problem details for each card
                        val cardsWithProblems = cards.mapNotNull { card ->
                            val problem = repository.getProblem(card.problemId)
                            problem?.let { RevisionCardWithProblem(card, it) }
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                userId = userId,
                                revisionCards = cardsWithProblems,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load revision cards"
                    )
                }
            }
        }
    }

    fun completeRevision(cardId: Long, rating: RevisionRating) {
        viewModelScope.launch {
            try {
                val userId = _uiState.value.userId ?: return@launch
                val card = _uiState.value.revisionCards.find { it.card.id == cardId }
                    ?: return@launch

                // Schedule next revision
                scheduleRevisionCardUseCase(
                    userId = userId,
                    problemId = card.card.problemId,
                    rating = rating
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to complete revision: ${e.message}")
                }
            }
        }
    }

    fun refresh() {
        loadRevisionCards()
    }
}

/**
 * UI State for Revision Screen
 */
data class RevisionUiState(
    val isLoading: Boolean = true,
    val userId: Long? = null,
    val revisionCards: List<RevisionCardWithProblem> = emptyList(),
    val error: String? = null
) {
    val dueCount: Int
        get() = revisionCards.size

    val hasDueCards: Boolean
        get() = revisionCards.isNotEmpty()
}

/**
 * Revision card with associated problem details
 */
data class RevisionCardWithProblem(
    val card: RevisionCardEntity,
    val problem: ProblemEntity
)
