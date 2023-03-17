package com.marine.fishtank.api

import com.marine.fishtank.model.DeviceState
import com.marine.fishtank.model.PeriodicTask
import com.marine.fishtank.model.Temperature
import com.skydoves.sandwich.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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

    fun enableBoardLed(enable: Boolean): Flow<Int> = flow {
        token?.let {
            emit(fishService.enableBoardLed(it, enable))
        } ?: emit(RESULT_FAIL_AUTH)
    }

    fun readDBTemperature(days: Int): Flow<List<Temperature>> = flow {
        token?.let {
            emit(fishService.readDBTemperature(it, days))
        } ?: emit(emptyList())
    }

    fun enableOutWater(enable: Boolean): Flow<Int> = flow {
        token?.let {
            emit(fishService.enableOutWater(it, enable))
        } ?: emit(RESULT_FAIL_HTTP)
    }

    fun enableOutWater2(enable: Boolean): Flow<Int> = flow {
        token?.let {
            emit(fishService.enableOutWater2(it, enable))
        } ?: emit(RESULT_FAIL_HTTP)
    }

    fun enableInWater(enable: Boolean): Flow<Int> = flow {
        token?.let {
            emit(fishService.enableInWater(it, enable))
        } ?: emit(RESULT_FAIL_HTTP)
    }

    fun enableLight(enable: Boolean): Flow<Int> = flow {
        token?.let {
            emit(fishService.enableLight(it, enable))
        } ?: emit(RESULT_FAIL_HTTP)
    }

    fun enablePurifier(enable: Boolean): Flow<Int> = flow {
        token?.let {
            emit(fishService.enablePurifier(it, enable))
        } ?: emit(RESULT_FAIL_HTTP)
    }

    fun enableHeater(enable: Boolean): Flow<Int> = flow {
        token?.let {
            emit(fishService.enableHeater(it, enable))
        } ?: emit(RESULT_FAIL_HTTP)
    }

    fun readHeaterState(): Flow<Boolean> = flow {
        token?.let {
            emit(fishService.readHeaterState(it))
        } ?: emit(false)
    }

    fun readInWaterState(): Flow<Boolean> = flow {
        token?.let {
            emit(fishService.readInWaterState(it))
        } ?: emit(false)
    }

    fun readOutWaterState(): Flow<Boolean> = flow {
        token?.let {
            emit(fishService.readOutWaterState(it))
        } ?: emit(false)
    }

    fun readOutWaterState2(): Flow<Boolean> = flow {
        token?.let {
            emit(fishService.readOutWaterState2(it))
        } ?: emit(false)
    }

    fun replaceWater(percentage: Float): Flow<Int> = flow {
        token?.let {
            emit(fishService.replaceWater(it, percentage))
        } ?: emit(RESULT_FAIL_HTTP)
    }

    fun changeLightBrightness(percentage: Float): Flow<Boolean> = flow {
        token?.let {
            emit(fishService.changeBrightness(it, percentage))
        } ?: emit(false)
    }

    fun readLightBrightness(): Flow<Float> = flow {
        token?.let {
            emit(fishService.readLightBrightness(it))
        } ?: emit(0f)
    }

    fun readAllState(): Flow<DeviceState> = flow {
        token?.let {
            emit(fishService.readAllState(it))
        } ?: emit(DeviceState())
    }

    fun fetchPeriodicTasks(): Flow<List<PeriodicTask>> = flow {
        token?.let {
            emit(fishService.fetchPeriodicTasks(it))
        } ?: emit(emptyList())
    }

    fun addPeriodicTask(periodicTask: PeriodicTask): Flow<Boolean> = flow {
        token?.let {
            emit(
                fishService.addPeriodicTask(
                    it, periodicTask.type, periodicTask.data, periodicTask.time
                )
            )
        } ?: emit(false)
    }

    fun deletePeriodicTask(id: Int): Flow<Boolean> = flow {
        token?.let {
            emit(fishService.deletePeriodicTask(it, id))
        } ?: emit(false)
    }

    fun reconnect() = flow<Void> {
        token?.let {
            fishService.reconnect(it)
        }
    }
}