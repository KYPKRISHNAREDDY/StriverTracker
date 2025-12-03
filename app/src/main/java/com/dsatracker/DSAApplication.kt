package com.dsatracker

import android.app.Application
import com.dsatracker.data.preferences.PreferencesManager
import com.dsatracker.domain.usecase.SeedDatabaseUseCase
import com.dsatracker.utils.NotificationHelper
import com.dsatracker.utils.NotificationScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class DSAApplication : Application() {

    @Inject
    lateinit var seedDatabaseUseCase: SeedDatabaseUseCase

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        // Initialize database with seed data on first launch
        applicationScope.launch {
            try {
                val isFirstLaunch = preferencesManager.getIsFirstLaunch()
                val isDataSeeded = preferencesManager.getIsDataSeeded()

                if (isFirstLaunch || !isDataSeeded) {
                    // Seed the database with initial data
                    val result = seedDatabaseUseCase()

                    if (result.isSuccess) {
                        // Data seeding successful
                        android.util.Log.d("DSAApplication", "Database seeded successfully")
                    } else {
                        // Handle seeding failure
                        android.util.Log.e("DSAApplication", "Database seeding failed", result.exceptionOrNull())
                    }
                }

                // Schedule notifications based on preferences
                scheduleNotifications()
            } catch (e: Exception) {
                android.util.Log.e("DSAApplication", "Error during initialization", e)
            }
        }
    }

    private suspend fun scheduleNotifications() {
        try {
            val isDailyEnabled = preferencesManager.isDailyReminderEnabled.first()
            val dailyHour = preferencesManager.getDailyReminderHour().toInt()
            val isWeeklyEnabled = preferencesManager.isWeeklySummaryEnabled.first()

            NotificationScheduler.rescheduleAll(
                context = this,
                isDailyEnabled = isDailyEnabled,
                dailyHour = dailyHour,
                isWeeklyEnabled = isWeeklyEnabled
            )

            android.util.Log.d("DSAApplication", "Notifications scheduled successfully")
        } catch (e: Exception) {
            android.util.Log.e("DSAApplication", "Error scheduling notifications", e)
        }
    }
}
