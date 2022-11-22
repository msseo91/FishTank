package com.marine.fishtank

import android.app.Application

class FishApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        INSTANCE = this
    }

    companion object {
        var INSTANCE: Application? = null
    }

}