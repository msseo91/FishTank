package com.marine.fishtank

import android.app.Application
import android.content.Intent
import android.widget.Toast
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.LogcatLogStrategy
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FishApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Set Logger setting.
        Logger.addLogAdapter(
            AndroidLogAdapter(
                PrettyFormatStrategy.newBuilder()
                    .methodCount(0) // (Optional) How many method line to show. Default 2
                    .tag("FishTank") // (Optional) Global tag for every log. Default PRETTY_LOGGER
                    .build()
            )
        )

        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Logger.e("Exception on ${thread.name}(${thread.id})$exception")

            startActivity(
                Intent(this, ErrorActivity::class.java)
                    .putExtra(KEY_MESSAGE, exception.stackTraceToString())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

}