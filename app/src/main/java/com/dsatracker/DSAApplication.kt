package com.dsatracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DSAApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Any initialization code here
    }
}
