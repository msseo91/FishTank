package com.marine.fishtank.model

import com.google.gson.Gson

// For test, mega2560 led control.
const val OP_MEGA_LED = 10

const val OP_GET_STATUS_ALL = 100
const val OP_GET_HISTORY = 101
const val OP_LISTEN_STATUS = 102

data class FishPacket(
    val opCode: Int,
    val data: Int
)

fun FishPacket.toJson() = Gson().toJson(this)
