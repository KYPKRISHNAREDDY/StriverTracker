package com.dsatracker

import android.app.Application
import com.dsatracker.data.preferences.PreferencesManager
import com.dsatracker.domain.usecase.SeedDatabaseUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
            } catch (e: Exception) {
                android.util.Log.e("DSAApplication", "Error during initialization", e)
            }
        }
    }
}
