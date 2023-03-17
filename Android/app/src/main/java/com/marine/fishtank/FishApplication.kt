package com.marine.fishtank

import android.app.Application
import android.content.Intent
import com.marine.fishtank.logger.PlainLogger
import com.orhanobut.logger.AndroidLogAdapter
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
                PlainLogger()
            )
        )

        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Logger.e("Exception on ${thread.name}(${thread.id})$exception")
            exception.printStackTrace()

            startActivity(
                Intent(this, ErrorActivity::class.java)
                    .putExtra(KEY_MESSAGE, exception.stackTraceToString())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

}