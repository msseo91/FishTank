package com.marine.fishtank.api

import com.marine.fishtank.model.PeriodicTask
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton


private const val TAG = "TankApi"

const val RESULT_FAIL_GENERAL = 0
const val RESULT_SUCCESS = 100
const val RESULT_FAIL_AUTH = 1
const val RESULT_FAIL_DEVICE_CONNECTION = 2
const val RESULT_FAIL_HTTP = 3

@Singleton
class TankDataSource @Inject constructor(
    private val fishService: FishService
) {

    private var token: String = ""

    suspend fun signIn(id: String, password: String) =
        fishService.signIn(id, password)
            .asTankResult()
            .onEach { if(it is TankResult.Success) token = it.data }

    fun readDBTemperature(days: Int) = flow { emitAll(fishService.readDBTemperature(token, days).asTankResult()) }

    fun enableOutWater(enable: Boolean) = flow { emitAll(fishService.enableOutWater(token, enable).asTankResult()) }

    fun enableOutWater2(enable: Boolean) = flow { emitAll(fishService.enableOutWater2(token, enable).asTankResult()) }
    fun enableInWater(enable: Boolean) = flow { emitAll(fishService.enableInWater(token, enable).asTankResult()) }

    fun enableHeater(enable: Boolean) = flow {  emitAll(fishService.enableHeater(token, enable).asTankResult()) }

    fun enableLight(enable: Boolean) = flow { emitAll(fishService.enableLight(token, enable).asTankResult()) }

    fun readLightState() = flow { emitAll(fishService.readLightState(token).asTankResult()) }

    fun readHeaterState() = flow { emitAll(fishService.readHeaterState(token).asTankResult()) }

    fun readInWaterState() = flow {  emitAll(fishService.readInWaterState(token).asTankResult()) }

    fun readOutWaterState() = flow { emitAll(fishService.readOutWaterState(token).asTankResult()) }

    fun readOutWaterState2() = flow { emitAll(fishService.readOutWaterState2(token).asTankResult()) }

    fun readAllState() = flow { emitAll(fishService.readAllState(token).asTankResult()) }

    fun fetchPeriodicTasks() = flow { emitAll(fishService.fetchPeriodicTasks(token).asTankResult()) }

    fun addPeriodicTask(periodicTask: PeriodicTask) = flow {
        emitAll(
            fishService.addPeriodicTask(token, periodicTask.type, periodicTask.data, periodicTask.time).asTankResult()
        )
    }

    fun deletePeriodicTask(id: Int) = flow {emitAll(fishService.deletePeriodicTask(token, id).asTankResult()) }

}