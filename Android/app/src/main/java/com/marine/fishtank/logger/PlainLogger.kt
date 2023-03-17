package com.marine.fishtank.logger

import android.util.Log
import com.orhanobut.logger.FormatStrategy

class PlainLogger: FormatStrategy {
    override fun log(priority: Int, tag: String?, message: String) {
        val curThread = Thread.currentThread()

        val className = curThread.stackTrace.find {
            !it.className.endsWith("com.marine.fishtank.logger.PlainLogger") &&
            it.className.contains("com.marine.fishtank")
        }?.className?.substringAfterLast(".")
        val threadName = curThread.name

        Log.println(priority, className, "t=$threadName msg=$message")
    }
}