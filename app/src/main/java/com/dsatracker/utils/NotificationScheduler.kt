package com.dsatracker.utils

import android.content.Context
import androidx.work.*
import com.dsatracker.workers.DailyReminderWorker
import com.dsatracker.workers.WeeklySummaryWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Utility to schedule notification workers
 */
object NotificationScheduler {

    private const val DAILY_REMINDER_WORK_NAME = "daily_reminder_work"
    private const val WEEKLY_SUMMARY_WORK_NAME = "weekly_summary_work"

    /**
     * Schedule daily reminder at specified hour
     * @param context Application context
     * @param hour Hour of day (0-23)
     */
    fun scheduleDailyReminder(context: Context, hour: Int) {
        val workManager = WorkManager.getInstance(context)

        // Calculate initial delay to next occurrence of the hour
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        // If target time has passed today, schedule for tomorrow
        if (targetTime.before(currentTime)) {
            targetTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis

        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            DAILY_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            dailyWorkRequest
        )
    }

    /**
     * Schedule weekly summary (Sunday at 8 PM by default)
     * @param context Application context
     */
    fun scheduleWeeklySummary(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // Calculate delay to next Sunday at 8 PM
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20) // 8 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Set to next Sunday
            val currentDayOfWeek = get(Calendar.DAY_OF_WEEK)
            val daysUntilSunday = (Calendar.SUNDAY - currentDayOfWeek + 7) % 7

            if (daysUntilSunday == 0 && before(currentTime)) {
                // Today is Sunday but time has passed, schedule for next Sunday
                add(Calendar.DAY_OF_YEAR, 7)
            } else if (daysUntilSunday > 0) {
                // Schedule for upcoming Sunday
                add(Calendar.DAY_OF_YEAR, daysUntilSunday)
            }
        }

        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis

        val weeklyWorkRequest = PeriodicWorkRequestBuilder<WeeklySummaryWorker>(
            repeatInterval = 7,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            WEEKLY_SUMMARY_WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            weeklyWorkRequest
        )
    }

    /**
     * Cancel daily reminder
     */
    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_REMINDER_WORK_NAME)
    }

    /**
     * Cancel weekly summary
     */
    fun cancelWeeklySummary(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WEEKLY_SUMMARY_WORK_NAME)
    }

    /**
     * Cancel all scheduled notifications
     */
    fun cancelAll(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(DAILY_REMINDER_WORK_NAME)
        workManager.cancelUniqueWork(WEEKLY_SUMMARY_WORK_NAME)
    }

    /**
     * Reschedule all notifications based on current settings
     */
    suspend fun rescheduleAll(
        context: Context,
        isDailyEnabled: Boolean,
        dailyHour: Int,
        isWeeklyEnabled: Boolean
    ) {
        // Cancel existing
        cancelAll(context)

        // Reschedule if enabled
        if (isDailyEnabled) {
            scheduleDailyReminder(context, dailyHour)
        }

        if (isWeeklyEnabled) {
            scheduleWeeklySummary(context)
        }
    }
}
