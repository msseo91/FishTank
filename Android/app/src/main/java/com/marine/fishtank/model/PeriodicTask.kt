package com.marine.fishtank.model

import android.content.Context
import android.os.Parcelable
import com.marine.fishtank.R
import com.marine.fishtank.model.PeriodicTask.Companion.TYPE_LIGHT
import com.marine.fishtank.model.PeriodicTask.Companion.TYPE_PUMP
import com.marine.fishtank.model.PeriodicTask.Companion.TYPE_PURIFIER
import com.marine.fishtank.model.PeriodicTask.Companion.TYPE_REPLACE_WATER
import com.marine.fishtank.model.PeriodicTask.Companion.TYPE_VALVE_IN_WATER
import com.marine.fishtank.model.PeriodicTask.Companion.TYPE_VALVE_OUT_WATER
import com.marine.fishtank.model.PeriodicTask.Companion.TYPE_VALVE_OUT_WATER_2
import kotlinx.parcelize.Parcelize

@Parcelize
data class PeriodicTask(
    var id: Int = 0,
    var userId: String = "",
    var type: Int = 0,
    var data: Int = 0,
    var time: String = ""
): Parcelable {
    companion object {
        const val TABLE_NAME = "periodicTask"
        const val COL_ID = "id"
        const val COL_USER_ID = "userId"
        const val COL_TYPE = "type"
        const val COL_DATA = "data"
        const val COL_TIME = "time"

        const val TYPE_REPLACE_WATER = 0
        const val TYPE_VALVE_OUT_WATER = 1
        const val TYPE_VALVE_IN_WATER = 2
        const val TYPE_PURIFIER = 3
        const val TYPE_LIGHT = 4
        const val TYPE_PUMP = 5
        const val TYPE_VALVE_OUT_WATER_2 = 6

        fun typeFromResource(resource: Int): Int {
            return when(resource) {
                R.string.in_valve -> TYPE_VALVE_IN_WATER
                R.string.out_valve -> TYPE_VALVE_OUT_WATER
                R.string.replace_water -> TYPE_REPLACE_WATER
                R.string.purifier -> TYPE_PURIFIER
                R.string.light_brightness -> TYPE_LIGHT
                else -> 0
            }
        }
    }
}

fun PeriodicTask.typeAsString(context: Context): String {
    return when(type) {
        TYPE_VALVE_OUT_WATER -> context.getString(R.string.out_valve)
        TYPE_VALVE_OUT_WATER_2 -> context.getString(R.string.out_valve2)
        TYPE_VALVE_IN_WATER -> context.getString(R.string.in_valve)
        TYPE_PURIFIER -> context.getString(R.string.purifier)
        TYPE_LIGHT -> context.getString(R.string.light_brightness)
        TYPE_PUMP -> context.getString(R.string.pump)
        else -> "UNKNOWN"
    }
}

fun PeriodicTask.valueAsText(context: Context): String {
    return when(type) {
        TYPE_VALVE_IN_WATER, TYPE_VALVE_OUT_WATER, TYPE_VALVE_OUT_WATER_2 -> {
            if(data == 1) {
                context.getString(R.string.open)
            } else {
                context.getString(R.string.close)
            }
        }
        TYPE_PURIFIER, TYPE_PUMP -> {
            if(data == 1) {
                context.getString(R.string.turn_on)
            } else {
                context.getString(R.string.turn_off)
            }
        }
        TYPE_LIGHT -> "$data%"

        else -> "UNKNOWN"
    }
}