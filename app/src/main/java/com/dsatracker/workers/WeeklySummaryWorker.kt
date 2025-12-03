package com.dsatracker.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dsatracker.data.preferences.PreferencesManager
import com.dsatracker.data.repository.DSARepository
import com.dsatracker.domain.usecase.CalculateStreakUseCase
import com.dsatracker.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Worker to show weekly summary notification
 * Scheduled weekly (e.g., Sunday evening)
 */
@HiltWorker
class WeeklySummaryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: DSARepository,
    private val preferencesManager: PreferencesManager,
    private val calculateStreakUseCase: CalculateStreakUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Check if weekly summary is enabled
            val isEnabled = preferencesManager.isWeeklySummaryEnabled.first()
            if (!isEnabled) {
                return Result.success()
            }

            // Get current user ID
            val userId = preferencesManager.getCurrentUserId()
                ?: return Result.success() // No user, skip

            // Get last 7 days stats
            val recentStats = repository.getRecentStats(userId, limit = 7).first()

            val weeklyProblemsSolved = recentStats.sumOf { it.problemsSolvedCount }
            val weeklyRevisionsCount = recentStats.sumOf { it.revisionsCount }

            // Calculate current streak
            val currentStreak = calculateStreakUseCase(userId)

            // Show notification
            NotificationHelper.showWeeklySummary(
                context = applicationContext,
                problemsSolved = weeklyProblemsSolved,
                revisionsDone = weeklyRevisionsCount,
                currentStreak = currentStreak
            )

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("WeeklySummaryWorker", "Error showing weekly summary", e)
            Result.failure()
        }
    }
}
