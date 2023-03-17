package com.marine.fishtank.logger

import android.util.Log
import com.orhanobut.logger.FormatStrategy

class PlainLogger: FormatStrategy {
    override fun log(priority: Int, tag: String?, message: String) {
        val className = Exception().stackTrace[1].className.substringAfterLast(".")
        val threadName = Thread.currentThread().name

        Log.println(priority, className, "t=$threadName msg=$message")
    }
}