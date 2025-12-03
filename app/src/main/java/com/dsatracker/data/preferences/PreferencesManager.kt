package com.dsatracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dsa_tracker_prefs")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val KEY_IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        private val KEY_IS_DATA_SEEDED = booleanPreferencesKey("is_data_seeded")
        private val KEY_CURRENT_USER_ID = longPreferencesKey("current_user_id")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        private val KEY_DAILY_REMINDER_HOUR = longPreferencesKey("daily_reminder_hour")
        private val KEY_WEEKLY_SUMMARY_ENABLED = booleanPreferencesKey("weekly_summary_enabled")
    }

    // First Launch
    val isFirstLaunch: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_IS_FIRST_LAUNCH] ?: true
    }

    suspend fun setFirstLaunchCompleted() {
        dataStore.edit { preferences ->
            preferences[KEY_IS_FIRST_LAUNCH] = false
        }
    }

    suspend fun getIsFirstLaunch(): Boolean {
        return dataStore.data.first()[KEY_IS_FIRST_LAUNCH] ?: true
    }

    // Data Seeding
    val isDataSeeded: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_IS_DATA_SEEDED] ?: false
    }

    suspend fun setDataSeeded(seeded: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_DATA_SEEDED] = seeded
        }
    }

    suspend fun getIsDataSeeded(): Boolean {
        return dataStore.data.first()[KEY_IS_DATA_SEEDED] ?: false
    }

    // Current User ID
    val currentUserId: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[KEY_CURRENT_USER_ID]
    }

    suspend fun setCurrentUserId(userId: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_CURRENT_USER_ID] = userId
        }
    }

    suspend fun getCurrentUserId(): Long? {
        return dataStore.data.first()[KEY_CURRENT_USER_ID]
    }

    // Onboarding
    val isOnboardingCompleted: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun getIsOnboardingCompleted(): Boolean {
        return dataStore.data.first()[KEY_ONBOARDING_COMPLETED] ?: false
    }

    // Notification Settings
    val isDailyReminderEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_DAILY_REMINDER_ENABLED] ?: true
    }

    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_DAILY_REMINDER_ENABLED] = enabled
        }
    }

    suspend fun getDailyReminderHour(): Long {
        return dataStore.data.first()[KEY_DAILY_REMINDER_HOUR] ?: 9L // Default 9 AM
    }

    suspend fun setDailyReminderHour(hour: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_DAILY_REMINDER_HOUR] = hour
        }
    }

    val isWeeklySummaryEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_WEEKLY_SUMMARY_ENABLED] ?: true
    }

    suspend fun setWeeklySummaryEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_WEEKLY_SUMMARY_ENABLED] = enabled
        }
    }

    // Clear all data (for testing or reset)
    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
