package com.marine.fishtank.api

import com.marine.fishtank.model.DeviceState
import com.marine.fishtank.model.PeriodicTask
import com.marine.fishtank.model.Temperature
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

private const val KEY_TOKEN = "token"
private const val KEY_ENABLE = "enable"
private const val KEY_ID = "id"
private const val KEY_PASSWORD = "password"
private const val KEY_DAYS = "days"
private const val KEY_PERCENTAGE = "percentage"
private const val KEY_PERIODIC = "periodicTask"
private const val KEY_TYPE = "type"
private const val KEY_DATA = "data"
private const val KEY_TIME = "time"

interface FishService {
    /**
     * @return user token.
     */
    @POST("/fish/signin")
    @FormUrlEncoded
    suspend fun signIn(@Field(KEY_ID) id: String, @Field(KEY_PASSWORD) password: String): ApiResponse<String>

    @POST("/fish/boardLed")
    @FormUrlEncoded
    suspend fun enableBoardLed(@Field(KEY_TOKEN) token: String, @Field(KEY_ENABLE) enable: Boolean): Int

    @POST("/fish/readDBTemperature")
    @FormUrlEncoded
    suspend fun readDBTemperature(@Field(KEY_TOKEN) token: String, @Field(KEY_DAYS) days: Int): List<Temperature>

    @POST("/fish/outWater")
    @FormUrlEncoded
    suspend fun enableOutWater(@Field(KEY_TOKEN) token: String, @Field(KEY_ENABLE) enable: Boolean): Int

    @POST("/fish/outWater2")
    @FormUrlEncoded
    suspend fun enableOutWater2(@Field(KEY_TOKEN) token: String, @Field(KEY_ENABLE) enable: Boolean): Int

    @POST("/fish/inWater")
    @FormUrlEncoded
    suspend fun enableInWater(@Field(KEY_TOKEN) token: String, @Field(KEY_ENABLE) enable: Boolean): Int

    @POST("/fish/light")
    @FormUrlEncoded
    suspend fun enableLight(@Field(KEY_TOKEN) token: String, @Field(KEY_ENABLE) enable: Boolean): Int

    @POST("/fish/purifier")
    @FormUrlEncoded
    suspend fun enablePurifier(@Field(KEY_TOKEN) token: String, @Field(KEY_ENABLE) enable: Boolean): Int

    @POST("/fish/heater")
    @FormUrlEncoded
    suspend fun enableHeater(@Field(KEY_TOKEN) token: String, @Field(KEY_ENABLE) enable: Boolean): Int

    @POST("/fish/read/heater")
    @FormUrlEncoded
    suspend fun readHeaterState(@Field(KEY_TOKEN) token: String): Boolean

    @POST("/fish/read/inWater")
    @FormUrlEncoded
    suspend fun readInWaterState(@Field(KEY_TOKEN) token: String): Boolean

    @POST("/fish/read/outWater")
    @FormUrlEncoded
    suspend fun readOutWaterState(@Field(KEY_TOKEN) token: String): Boolean

    @POST("/fish/read/outWater2")
    @FormUrlEncoded
    suspend fun readOutWaterState2(@Field(KEY_TOKEN) token: String): Boolean

    @POST("/fish/suspend func/replaceWater")
    @FormUrlEncoded
    suspend fun replaceWater(@Field(KEY_TOKEN) token: String, @Field(KEY_PERCENTAGE) percentage: Float): Int

    @POST("/fish/brightness")
    @FormUrlEncoded
    suspend fun changeBrightness(@Field(KEY_TOKEN) token: String, @Field(KEY_PERCENTAGE) percentage: Float): Boolean

    @POST("/fish/brightness/read")
    @FormUrlEncoded
    suspend fun readLightBrightness(@Field(KEY_TOKEN) token: String): Float

    @POST("/fish/read/allState")
    @FormUrlEncoded
    suspend fun readAllState(@Field(KEY_TOKEN) token: String): DeviceState

    @POST("/fish/periodic/add")
    @FormUrlEncoded
    suspend fun addPeriodicTask(
        @Field(KEY_TOKEN) token: String,
        @Field(KEY_TYPE) type: Int,
        @Field(KEY_DATA) data: Int,
        @Field(KEY_TIME) time: String
    ): Boolean

    @POST("/fish/periodic/fetch")
    @FormUrlEncoded
    suspend fun fetchPeriodicTasks(@Field(KEY_TOKEN) token: String): List<PeriodicTask>

    @POST("/fish/periodic/delete")
    @FormUrlEncoded
    suspend fun deletePeriodicTask(
        @Field(KEY_TOKEN) token: String,
        @Field(KEY_PERIODIC) taskId: Int
    ): Boolean

    @POST("/fish/reconnect")
    @FormUrlEncoded
    suspend fun reconnect(@Field(KEY_TOKEN) token: String)
}