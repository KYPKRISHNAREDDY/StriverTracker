package com.dsatracker.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dsatracker.data.preferences.PreferencesManager
import com.dsatracker.data.repository.DSARepository
import com.dsatracker.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Worker to show daily reminder notification
 * Scheduled daily at user's chosen time
 */
@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: DSARepository,
    private val preferencesManager: PreferencesManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Check if daily reminder is enabled
            val isEnabled = preferencesManager.isDailyReminderEnabled.first()
            if (!isEnabled) {
                return Result.success()
            }

            // Get current user ID
            val userId = preferencesManager.getCurrentUserId()
                ?: return Result.success() // No user, skip

            // Get pending problems count
            val solvedCount = repository.getSolvedCountFlow(userId).first()
            val totalProblems = repository.getTotalProblemsCount("STRIVER_A2Z")
            val pendingProblems = totalProblems - solvedCount

            // Get due revisions count
            val endDate = System.currentTimeMillis()
            val dueRevisions = repository.getDueRevisionCards(userId, endDate, limit = 15).first().size

            // Show notification
            NotificationHelper.showDailyReminder(
                context = applicationContext,
                pendingProblems = pendingProblems,
                dueRevisions = dueRevisions
            )

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("DailyReminderWorker", "Error showing daily reminder", e)
            Result.failure()
        }
    }
}
