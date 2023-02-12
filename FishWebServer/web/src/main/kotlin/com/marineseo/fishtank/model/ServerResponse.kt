package com.marineseo.fishtank.model

import com.google.gson.Gson

const val RESULT_FAIL_GENERAL = 0
const val RESULT_SUCCESS = 100
const val RESULT_FAIL_AUTH = 1
const val RESULT_FAIL_DEVICE_CONNECTION = 2

data class ServerResponse(
    val token: String,
    val result: Int
) {
    companion object {
        fun createFromJson(json: String): ServerResponse {
            val gson = Gson()
            return gson.fromJson(json, ServerResponse::class.java)
        }
    }
}

fun ServerResponse.toJson(): String = Gson().toJson(this)
