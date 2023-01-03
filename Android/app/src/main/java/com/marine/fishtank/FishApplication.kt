package com.marine.fishtank

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FishApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        INSTANCE = this
    }

    companion object {
        var INSTANCE: Application? = null
    }

}