package com.marine.fishtank.api

import com.marine.fishtank.model.PeriodicTask
import com.marine.fishtank.model.Temperature
import retrofit2.Call
import retrofit2.http.Body
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
    fun signIn(@Field(KEY_ID) id: String, @Field(KEY_PASSWORD) password: String): Call<String>

    @POST("/fish/boardLed")
    @FormUrlEncoded
    fun enableBoardLed(@Field(KEY_TOKEN) token: String, @Field(KEY_ENABLE) enable: Boolean): Call<Int>

    @POST("/fish/readDBTemperature")
    @FormUrlEncoded
    fun readDBTemperature(@Field(KEY_TOKEN) token: String, @Field(KEY_DAYS) days: Int): Call<List<Temperature>>

    @POST("/fish/outWater")
    @FormUrlEncoded
    fun enableOutWater(@Field(KEY_TOKEN) token: String, @Field(KEY_ENABLE) enable: Boolean): Call<Int>

    @POST("/fish/inWater")
    @FormUrlEncoded
    fun enableInWater(@Field(KEY_TOKEN) token: String, @Field(KEY_ENABLE) enable: Boolean): Call<Int>

    @POST("/fish/light")
    @FormUrlEncoded
    fun enableLight(@Field(KEY_TOKEN) token: String, @Field(KEY_ENABLE) enable: Boolean): Call<Int>

    @POST("/fish/purifier")
    @FormUrlEncoded
    fun enablePurifier(@Field(KEY_TOKEN) token: String, @Field(KEY_ENABLE) enable: Boolean): Call<Int>

    @POST("/fish/heater")
    @FormUrlEncoded
    fun enableHeater(@Field(KEY_TOKEN) token: String, @Field(KEY_ENABLE) enable: Boolean): Call<Int>

    @POST("/fish/read/heater")
    @FormUrlEncoded
    fun readHeaterState(@Field(KEY_TOKEN) token: String): Call<Boolean>

    @POST("/fish/read/inWater")
    @FormUrlEncoded
    fun readInWaterState(@Field(KEY_TOKEN) token: String): Call<Boolean>

    @POST("/fish/read/outWater")
    @FormUrlEncoded
    fun readOutWaterState(@Field(KEY_TOKEN) token: String): Call<Boolean>

    @POST("/fish/func/replaceWater")
    @FormUrlEncoded
    fun replaceWater(@Field(KEY_TOKEN) token: String, @Field(KEY_PERCENTAGE) percentage: Float): Call<Int>

    @POST("/fish/brightness")
    @FormUrlEncoded
    fun changeBrightness(@Field(KEY_TOKEN) token: String, @Field(KEY_PERCENTAGE) percentage: Float): Call<Boolean>

    @POST("/fish/brightness/read")
    @FormUrlEncoded
    fun readLightBrightness(@Field(KEY_TOKEN) token: String): Call<Float>

    @POST("/fish/periodic/add")
    @FormUrlEncoded
    fun addPeriodicTask(
        @Field(KEY_TOKEN) token: String,
        @Field(KEY_TYPE) type: Int,
        @Field(KEY_DATA) data: Int,
        @Field(KEY_TIME) time: String
    ): Call<Boolean>

    @POST("/fish/periodic/fetch")
    @FormUrlEncoded
    fun fetchPeriodicTasks(@Field(KEY_TOKEN) token: String): Call<List<PeriodicTask>>

    @POST("/fish/periodic/delete")
    @FormUrlEncoded
    fun deletePeriodicTask(
        @Field(KEY_TOKEN) token: String,
        @Field(KEY_PERIODIC) taskId: Int
    ): Call<Boolean>

    @POST("/fish/reconnect")
    @FormUrlEncoded
    fun reconnect(@Field(KEY_TOKEN) token: String): Call<Void>
}