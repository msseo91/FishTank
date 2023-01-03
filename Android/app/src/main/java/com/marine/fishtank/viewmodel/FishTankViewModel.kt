package com.marine.fishtank.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.marine.fishtank.ConnectionSetting
import com.marine.fishtank.DEFAULT_CONNECTION_SETTING
import com.marine.fishtank.api.TankDataSource
import com.marine.fishtank.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "FishTankViewModel"

@HiltViewModel
class FishTankViewModel @Inject constructor(
    private val tankDataSource: TankDataSource
) : ViewModel() {
    val temperatureLiveData = MutableLiveData<List<Temperature>>()
    val periodicTaskLiveData = MutableLiveData<List<PeriodicTask>>()

    private val _uiState = MutableLiveData<UiState>().apply { value = UiState() }
    val uiState: LiveData<UiState>
        get() = _uiState

    var isRefreshing = MutableLiveData<Boolean>(false)
    val message = MutableLiveData<String>()

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
            val inWaterState = tankDataSource.readInWaterState()
            val outWaterState = tankDataSource.readOutWaterState()
            val brightness = tankDataSource.readLightBrightness()
            val heaterState = tankDataSource.readHeaterState()

            Log.d(TAG, "readState - inWaterState=$inWaterState, outWaterState=$outWaterState," +
                    "Heater=$heaterState, brightness=$brightness")

            _uiState.postValue(
                _uiState.value?.copy(
                    inWaterValveState = inWaterState,
                    outWaterValveState = outWaterState,
                    brightness = brightness.toInt(),
                    heaterState = heaterState
                )
            )
        }
    }

    fun startFetchTemperature(days: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            temperatureLiveData.postValue(
                tankDataSource.readDBTemperature(days)
            )
        }
    }

    fun fetchPeriodicTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            periodicTaskLiveData.postValue(
                tankDataSource.fetchPeriodicTasks()
            )
        }
    }

    fun uiEvent(uiEvent: UiEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            when (uiEvent) {
                is UiEvent.OutWaterEvent -> {
                    tankDataSource.enableOutWater(uiEvent.value)
                    readState()
                }
                is UiEvent.InWaterEvent -> {
                    tankDataSource.enableInWater(uiEvent.value)
                    readState()
                }
                is UiEvent.LedEvent -> {
                    tankDataSource.enableBoardLed(uiEvent.value)
                    //readState()
                }
                is UiEvent.HeaterEvent -> {
                    tankDataSource.enableHeater(uiEvent.value)
                    readState()
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
                            tankDataSource.changeLightBrightness(uiEvent.brightness * 0.01f)
                        }
                    }
                }
                is UiEvent.AddPeriodicTask -> {
                    Log.d(TAG, "Add periodicTask! ${uiEvent.periodicTask}")
                    tankDataSource.addPeriodicTask(uiEvent.periodicTask)
                    fetchPeriodicTasks()
                }
                is UiEvent.DeletePeriodicTask -> {
                    val result = tankDataSource.deletePeriodicTask(uiEvent.periodicTask)
                    message.postValue("Delete task result=$result")
                    fetchPeriodicTasks()
                }
                is UiEvent.TryReconnect -> {
                    tankDataSource.reconnect()
                }
                else -> {
                    Log.e(TAG, "This event is not supported anymore. ($uiEvent)")
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
    class DeletePeriodicTask(val periodicTask: PeriodicTask): UiEvent()

    class TryReconnect(): UiEvent()
}