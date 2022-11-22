package com.marine.fishtank.model

import com.google.gson.Gson

// For test, mega2560 led control.
const val SERVER_OP_MEGA_LED = 10
const val SERVER_OP_SIGN_IN = 50

const val SERVER_OP_GET_HISTORY = 100
const val SERVER_OP_LISTEN_STATUS = 101
const val SERVER_OP_READ_TEMPERATURE = 102
const val SERVER_OP_DB_TEMPERATURE = 103

const val SERVER_OP_WATER_PUMP = 200
const val SERVER_OP_OUT_WATER = 201
const val SERVER_OP_IN_WATER = 202
const val SERVER_OP_LIGHT = 203
const val SERVER_OP_PURIFIER_1 = 204
const val SERVER_OP_PURIFIER_2 = 205
const val SERVER_OP_HEATER = 206
const val SERVER_OP_READ_IN_WATER = 207
const val SERVER_OP_READ_OUT_WATER = 208

const val SERVER_OP_WATER_REPLACE = 300

data class ServerPacket(
    val clientId: Int = 0,
    val opCode: Int,
    val data: Int = 0,
    val pinState: Boolean = false,
    val temperatureList: List<Temperature> = emptyList(),
    val obj: Any = Any()
) {
    companion object {
        fun createFromJson(json: String) = Gson().fromJson(json, ServerPacket::class.java)
    }
}

fun ServerPacket.toJson(): String = Gson().toJson(this)
