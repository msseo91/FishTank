package com.marine.fishtank.viewmodel

import androidx.compose.runtime.MutableState
import androidx.lifecycle.*
import com.marine.fishtank.ConnectionSetting
import com.marine.fishtank.DEFAULT_CONNECTION_SETTING
import com.marine.fishtank.api.TankDataSource
import com.marine.fishtank.model.*
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "FishTankViewModel"

@HiltViewModel
class FishTankViewModel @Inject constructor(
    private val tankDataSource: TankDataSource
) : ViewModel() {
    private val tankState = TankState()
    private val _tankStateFlow = MutableSharedFlow<DataSource<TankState>>(replay = 5)
    val tankStateFlow: SharedFlow<DataSource<TankState>> = _tankStateFlow

    private val _refreshFlow = MutableStateFlow(false)
    val refreshFlow: StateFlow<Boolean> = _refreshFlow

    private val _messageFlow = MutableStateFlow("")
    val messageFlow: StateFlow<String> = _messageFlow

    init {
        refreshState()
    }

    fun refreshState() {
        Logger.d("emit - refreshState - loading")
        readState()
        fetchPeriodicTasks()
        Logger.d("emit - refreshState - success")
    }

    private fun readState() {
        viewModelScope.launch {
            combine(
                tankDataSource.readInWaterState(),
                tankDataSource.readOutWaterState(),
                tankDataSource.readLightBrightness(),
                tankDataSource.readHeaterState(),
                tankDataSource.readDBTemperature(1)
            ) { inWaterState, outWaterState, brightness, heaterState, temperature ->
                tankState.apply {
                    inWaterValveState = inWaterState
                    outWaterValveState = outWaterState
                    this.brightness = brightness.toInt()
                    this.heaterState = heaterState
                    temperatureList.clear()
                    temperatureList.addAll(temperature)
                }
            }.collect {
                Logger.d("emit - readState $it")
                _tankStateFlow.emit(DataSource.success(it))
            }
        }
    }

    fun startFetchTemperature(days: Int) {
        viewModelScope.launch {
            tankDataSource.readDBTemperature(days).collect {
                Logger.d("emit - startFetchTemperature")
                _tankStateFlow.emit(DataSource.success(tankState.apply {
                    temperatureList.clear()
                    temperatureList.addAll(it)
                }))

            }
        }
    }

    private fun fetchPeriodicTasks() {
        viewModelScope.launch {
            tankDataSource.fetchPeriodicTasks().collect {
                Logger.d("emit - fetchPeriodicTasks")
                _tankStateFlow.emit(DataSource.success(tankState.apply {
                    periodicTaskList.clear()
                    periodicTaskList.addAll(it)
                }))
            }
        }
    }

    fun enableOutWater(enable: Boolean) = viewModelScope.launch {
        tankDataSource.enableOutWater(enable).collect { readState() }
    }

    fun enableInWater(enable: Boolean) = viewModelScope.launch {
        tankDataSource.enableInWater(enable).collect { readState() }
    }

    fun enableBoardLed(enable: Boolean) = viewModelScope.launch {
        tankDataSource.enableBoardLed(enable).collect()
    }

    fun enableHeater(enable: Boolean) = viewModelScope.launch {
        tankDataSource.enableHeater(enable).collect { readState() }
    }

    fun reconnect() = viewModelScope.launch {
        tankDataSource.reconnect().collect()
    }

    fun addPeriodicTask(periodicTask: PeriodicTask) = viewModelScope.launch {
        tankDataSource.addPeriodicTask(periodicTask).collect { fetchPeriodicTasks() }
    }

    fun deletePeriodicTask(periodicTask: PeriodicTask) = viewModelScope.launch {
        tankDataSource.deletePeriodicTask(periodicTask).collect { fetchPeriodicTasks() }
    }

    fun changeLightBrightness(brightness: Int) = viewModelScope.launch {
        tankDataSource.changeLightBrightness(brightness * 0.01f).collect()
    }
}

data class TankState(
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
    var brightness: Int = 0,

    val periodicTaskList: MutableList<PeriodicTask> = mutableListOf(),
    val temperatureList: MutableList<Temperature> = mutableListOf()
)
