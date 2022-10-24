package com.marine.fishtank.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.marine.fishtank.BuildConfig
import com.marine.fishtank.ConnectionSetting
import com.marine.fishtank.DEFAULT_CONNECTION_SETTING
import com.marine.fishtank.api.TankApi
import com.marine.fishtank.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

private const val TAG = "FishTankViewModel"

class FishTankViewModel(application: Application) : AndroidViewModel(application) {
    val temperatureLiveData = MutableLiveData<List<Temperature>>()
    val periodicTaskLiveData = MutableLiveData<List<PeriodicTask>>()

    private val tankApi: TankApi = TankApi.getInstance(BuildConfig.SERVER_URL)

    private val _uiState = MutableLiveData<UiState>().apply { value = UiState() }
    val uiState: LiveData<UiState>
        get() = _uiState

    var isRefreshing = MutableLiveData<Boolean>(false)

    fun refreshState() {
        viewModelScope.launch {
            isRefreshing.value = true
            withContext(Dispatchers.IO) {
                readState()
                startFetchTemperature(1)
                fetchPeriodicTasks()
            }
            isRefreshing.value = false
        }
    }

    fun readState() {
        viewModelScope.launch(Dispatchers.IO) {
            val inWaterState = tankApi.readInWaterState()
            val outWaterState = tankApi.readOutWaterState()
            val brightness = tankApi.readLightBrightness()

            Log.d(TAG, "readState - inWaterState=$inWaterState, outWaterState=$outWaterState, brightness=$brightness")

            _uiState.postValue(
                _uiState.value?.copy(
                    inWaterValveState = inWaterState,
                    outWaterValveState = outWaterState,
                    brightness = brightness.toInt()
                )
            )
        }
    }

    fun startFetchTemperature(days: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            temperatureLiveData.postValue(
                tankApi.readDBTemperature(days)
            )
        }
    }

    fun fetchPeriodicTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            periodicTaskLiveData.postValue(
                tankApi.fetchPeriodicTasks()
            )
        }
    }

    fun uiEvent(uiEvent: UiEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            when (uiEvent) {
                is UiEvent.OutWaterEvent -> {
                    tankApi.enableOutWater(uiEvent.value)
                    readState()
                }
                is UiEvent.InWaterEvent -> {
                    tankApi.enableInWater(uiEvent.value)
                    readState()
                }
                is UiEvent.LightEvent -> {
                    tankApi.enableLight(uiEvent.value)
                    readState()
                }
                is UiEvent.HeaterEvent -> {
                    tankApi.enableHeater(uiEvent.value)
                    readState()
                }
                is UiEvent.PurifierEvent -> {
                    tankApi.enablePurifier(uiEvent.value)
                    readState()
                }
                is UiEvent.ReplaceWater -> {
                    tankApi.replaceWater(uiEvent.ratio * 0.01F)
                    readState()
                }
                is UiEvent.LedEvent -> {
                    tankApi.enableBoardLed(uiEvent.value)
                    //readState()
                }
                is UiEvent.OnChangeTemperatureRange -> {
                    startFetchTemperature(uiEvent.intValue)
                }
                is UiEvent.OnLightBrightnessChange -> {
                    Log.d(TAG, "OnLightBrightnessChange=${uiEvent.brightness}")
                    // First, post the value.
                    withContext(Dispatchers.Main) {
                        _uiState.value = uiState.value?.copy(brightness = uiEvent.brightness)
                    }

                    if (uiEvent.adjust) {
                        Log.d(TAG, "Request adjust brightness to ${uiEvent.brightness}")
                        viewModelScope.launch(Dispatchers.IO) {
                            tankApi.changeLightBrightness(uiEvent.brightness * 0.01f)
                        }
                    }
                }
                is UiEvent.AddPeriodicTask -> {
                    Log.d(TAG, "Add periodicTask! ${uiEvent.periodicTask}")
                    tankApi.addPeriodicTask(uiEvent.periodicTask)
                    tankApi.fetchPeriodicTasks()
                }
                is UiEvent.TryReconnect -> {
                    tankApi.reconnect()
                }
            }
        }
    }
}

data class UiState(
    var outWaterValveState: Boolean = false,
    var inWaterValveState: Boolean = false,
    var lightState: Boolean = false,
    var pumpState: Boolean = false,
    var heaterState: Boolean = false,
    var purifierState: Boolean = false,
    var ledState: Boolean = false,

    var temperature: Double = 0.0,
    var temperatureDays: Float = 0f,

    var resultText: String = "",

    var connectionSetting: ConnectionSetting = DEFAULT_CONNECTION_SETTING,
    var serverUrl: String = "",

    /**
     * Percentage of brightness.
     */
    var brightness: Int = 0
)

sealed class UiEvent(
    val value: Boolean = false,
    val intValue: Int = 0
) {
    class OutWaterEvent(enable: Boolean) : UiEvent(enable)
    class InWaterEvent(enable: Boolean) : UiEvent(enable)
    class LightEvent(enable: Boolean) : UiEvent(enable)
    class HeaterEvent(enable: Boolean) : UiEvent(enable)
    class PurifierEvent(enable: Boolean) : UiEvent(enable)
    class LedEvent(enable: Boolean) : UiEvent(enable)

    class ReplaceWater(val ratio: Int) : UiEvent()
    class OnChangeTemperatureRange(count: Int) : UiEvent(intValue = count)

    class OnLightBrightnessChange(val brightness: Int, val adjust: Boolean) : UiEvent()
    class AddPeriodicTask(val periodicTask: PeriodicTask): UiEvent()

    class TryReconnect(): UiEvent()
}