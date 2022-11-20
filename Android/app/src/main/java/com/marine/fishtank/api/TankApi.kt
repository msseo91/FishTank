package com.marine.fishtank.api

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.media.session.MediaSessionCompat.KEY_TOKEN
import android.util.Log
import com.marine.fishtank.model.*
import okhttp3.HttpUrl
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.net.HttpURLConnection
import java.security.InvalidParameterException


private const val TAG = "TankApi"

interface OnServerPacketListener {
    fun onServerPacket(packet: ServerPacket)
}

const val RESULT_FAIL_GENERAL = 0
const val RESULT_SUCCESS = 100
const val RESULT_FAIL_AUTH = 1
const val RESULT_FAIL_DEVICE_CONNECTION = 2
const val RESULT_FAIL_HTTP = 3

class TankApi(
    private val url: String
) {
    private val fishService = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(FishService::class.java)

    private var token: String? = null

    fun isAlreadySignIn(): Boolean {
        return token != null
    }

    fun signIn(id: String, password: String): Boolean {
        try {
            val call = fishService.signIn(id, password)
            val response = call.execute()
            if (response.code() == HttpURLConnection.HTTP_OK) {
                token = response.body()
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    fun enableBoardLed(enable: Boolean): Int {
        verifyTokenNull()

        try {
            val response = fishService.enableBoardLed(token!!, enable).execute()
            return response.body() ?: RESULT_FAIL_HTTP
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return RESULT_FAIL_HTTP
    }

    fun readDBTemperature(days: Int): List<Temperature> {
        verifyTokenNull()

        try {
            val response = fishService.readDBTemperature(token!!, days).execute()
            return response.body() ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    fun enableOutWater(enable: Boolean): Int {
        verifyTokenNull()

        try {
            val response = fishService.enableOutWater(token!!, enable).execute()
            return response.body() ?: RESULT_FAIL_HTTP
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return RESULT_FAIL_HTTP
    }

    fun enableInWater(enable: Boolean): Int {
        verifyTokenNull()

        try {
            val response = fishService.enableInWater(token!!, enable).execute()
            return response.body() ?: RESULT_FAIL_HTTP
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return RESULT_FAIL_HTTP
    }

    fun enableLight(enable: Boolean): Int {
        verifyTokenNull()

        try {
            val response = fishService.enableLight(token!!, enable).execute()
            return response.body() ?: RESULT_FAIL_HTTP
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return RESULT_FAIL_HTTP
    }

    fun enablePurifier(enable: Boolean): Int {
        verifyTokenNull()

        try {
            val response = fishService.enablePurifier(token!!, enable).execute()
            return response.body() ?: RESULT_FAIL_HTTP
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return RESULT_FAIL_HTTP
    }

    fun enableHeater(enable: Boolean): Int {
        verifyTokenNull()
        try {
            val response = fishService.enableHeater(token!!, enable).execute()
            return response.body() ?: RESULT_FAIL_HTTP
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return RESULT_FAIL_HTTP
    }

    fun readHeaterState(): Boolean {
        verifyTokenNull()
        try {
            val response = fishService.readHeaterState(token!!).execute()
            return response.body() ?: false
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun readInWaterState(): Boolean {
        verifyTokenNull()

        try {
            val response = fishService.readInWaterState(token!!).execute()

            return response.body() ?: false
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    fun readOutWaterState(): Boolean {
        verifyTokenNull()

        try {
            val response = fishService.readOutWaterState(token!!).execute()
            return response.body() ?: false
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun replaceWater(percentage: Float): Int {
        verifyTokenNull()

        try {
            val response = fishService.replaceWater(token!!, percentage).execute()
            return response.body() ?: RESULT_FAIL_HTTP
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return RESULT_FAIL_HTTP
    }

    fun changeLightBrightness(percentage: Float): Boolean {
        verifyTokenNull()
        try {
            val response = fishService.changeBrightness(token!!, percentage).execute()
            return response.body() ?: false
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    fun readLightBrightness(): Float {
        verifyTokenNull()
        try {
            return fishService.readLightBrightness(token!!).execute().body() ?: 0f
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return 0f
    }

    fun fetchPeriodicTasks(): List<PeriodicTask> {
        verifyTokenNull()
        try {
            return fishService.fetchPeriodicTasks(token!!).execute().body() ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return emptyList()
    }

    fun addPeriodicTask(periodicTask: PeriodicTask): Boolean {
        verifyTokenNull()
        try {
            return fishService.addPeriodicTask(
                token!!,
                periodicTask.type,
                periodicTask.data,
                periodicTask.time
            ).execute().body() ?: false
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    fun deletePeriodicTask(periodicTask: PeriodicTask): Boolean {
        verifyTokenNull()
        try {
            return fishService.deletePeriodicTask(token!!, periodicTask.id).execute().body() ?: false
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun reconnect() {
        verifyTokenNull()
        fishService.reconnect(token!!).execute().body()
    }

    private fun verifyTokenNull() {
        if (token == null) {
            throw IllegalStateException("Token is null! You must success sign-in first.")
        }
    }


    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: TankApi? = null

        fun getInstance(url: String): TankApi {
            return INSTANCE ?: synchronized(this) {
                INSTANCE?.let {
                    return it
                }

                val instance = TankApi(url)
                INSTANCE = instance
                instance
            }
        }
    }
}