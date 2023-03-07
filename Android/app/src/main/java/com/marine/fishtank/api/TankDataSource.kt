package com.marine.fishtank.api

import com.marine.fishtank.model.PeriodicTask
import com.marine.fishtank.model.Temperature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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

    private var token: String? = null

    fun isAlreadySignIn(): Boolean = !token.isNullOrEmpty()

    fun signIn(id: String, password: String): Flow<Boolean> = flow {
        token = fishService.signIn(id, password)
        emit(!token.isNullOrEmpty())
    }.flowOn(Dispatchers.IO)

    fun enableBoardLed(enable: Boolean): Flow<Int> = flow {
        token?.let {
            emit(fishService.enableBoardLed(it, enable))
        } ?: emit(RESULT_FAIL_AUTH)
    }.flowOn(Dispatchers.IO)

    fun readDBTemperature(days: Int): Flow<List<Temperature>> = flow {
        token?.let {
            emit(fishService.readDBTemperature(it, days))
        } ?: emit(emptyList())
    }.flowOn(Dispatchers.IO)

    fun enableOutWater(enable: Boolean): Flow<Int> = flow {
        token?.let {
            emit(fishService.enableOutWater(it, enable))
        } ?: emit(RESULT_FAIL_HTTP)
    }.flowOn(Dispatchers.IO)

    fun enableOutWater2(enable: Boolean): Flow<Int> = flow {
        token?.let {
            emit(fishService.enableOutWater2(it, enable))
        } ?: emit(RESULT_FAIL_HTTP)
    }.flowOn(Dispatchers.IO)

    fun enableInWater(enable: Boolean): Flow<Int> = flow {
        token?.let {
            emit(fishService.enableInWater(it, enable))
        } ?: emit(RESULT_FAIL_HTTP)
    }.flowOn(Dispatchers.IO)

    fun enableLight(enable: Boolean): Flow<Int> = flow {
        token?.let {
            emit(fishService.enableLight(it, enable))
        } ?: emit(RESULT_FAIL_HTTP)
    }.flowOn(Dispatchers.IO)

    fun enablePurifier(enable: Boolean): Flow<Int> = flow {
        token?.let {
            emit(fishService.enablePurifier(it, enable))
        } ?: emit(RESULT_FAIL_HTTP)
    }.flowOn(Dispatchers.IO)

    fun enableHeater(enable: Boolean): Flow<Int> = flow {
        token?.let {
            emit(fishService.enableHeater(it, enable))
        } ?: emit(RESULT_FAIL_HTTP)
    }.flowOn(Dispatchers.IO)

    fun readHeaterState(): Flow<Boolean> = flow {
        token?.let {
            emit(fishService.readHeaterState(it))
        } ?: emit(false)
    }.flowOn(Dispatchers.IO)

    fun readInWaterState(): Flow<Boolean> = flow {
        token?.let {
            emit(fishService.readInWaterState(it))
        } ?: emit(false)
    }.flowOn(Dispatchers.IO)

    fun readOutWaterState(): Flow<Boolean> = flow {
        token?.let {
            emit(fishService.readOutWaterState(it))
        } ?: emit(false)
    }.flowOn(Dispatchers.IO)

    fun readOutWaterState2(): Flow<Boolean> = flow {
        token?.let {
            emit(fishService.readOutWaterState2(it))
        } ?: emit(false)
    }.flowOn(Dispatchers.IO)

    fun replaceWater(percentage: Float): Flow<Int> = flow {
        token?.let {
            emit(fishService.replaceWater(it, percentage))
        } ?: emit(RESULT_FAIL_HTTP)
    }.flowOn(Dispatchers.IO)

    fun changeLightBrightness(percentage: Float): Flow<Boolean> = flow {
        token?.let {
            emit(fishService.changeBrightness(it, percentage))
        } ?: emit(false)
    }.flowOn(Dispatchers.IO)

    fun readLightBrightness(): Flow<Float> = flow {
        token?.let {
            emit(fishService.readLightBrightness(it))
        } ?: emit(0f)
    }.flowOn(Dispatchers.IO)

    fun fetchPeriodicTasks(): Flow<List<PeriodicTask>> = flow {
        token?.let {
            emit(fishService.fetchPeriodicTasks(it))
        } ?: emit(emptyList())
    }.flowOn(Dispatchers.IO)

    fun addPeriodicTask(periodicTask: PeriodicTask): Flow<Boolean> = flow {
        token?.let {
            emit(
                fishService.addPeriodicTask(
                    it, periodicTask.type, periodicTask.data, periodicTask.time
                )
            )
        } ?: emit(false)
    }.flowOn(Dispatchers.IO)

    fun deletePeriodicTask(periodicTask: PeriodicTask): Flow<Boolean> = flow {
        token?.let {
            emit(fishService.deletePeriodicTask(it, periodicTask.id))
        } ?: emit(false)
    }.flowOn(Dispatchers.IO)

    fun reconnect() = flow<Void> {
        token?.let {
            fishService.reconnect(it)
        }
    }.flowOn(Dispatchers.IO)
}