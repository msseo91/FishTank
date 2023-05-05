package com.marine.fishtank.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeUtils {
    fun currentTimeHHmm() = SimpleDateFormat("HH:mm", Locale.US).format(Date())

    fun currentHourIn24Format() = SimpleDateFormat("HH", Locale.US).format(Date())

    fun currentHour() = Calendar.getInstance()[Calendar.HOUR_OF_DAY]

    fun currentMinute() = Calendar.getInstance()[Calendar.MINUTE]

    fun formatTimeHHmm(hour: Int, minute: Int) = "${if(hour < 10) "0$hour" else "$hour"}:${if(minute < 10) "0$minute" else "$minute"}"
}