package com.marine.fishtank.viewmodel

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
    private val _tankControlStateFlow = MutableStateFlow<DataSource<TankState>>(DataSource.loading(TankState()))
    val tankControlStateFlow: StateFlow<DataSource<TankState>> = _tankControlStateFlow

    private val _temperatureFlow = MutableStateFlow<DataSource<List<Temperature>>>(DataSource.loading(emptyList()))
    val temperatureFlow : StateFlow<DataSource<List<Temperature>>> = _temperatureFlow

    private val _periodicTaskFlow = MutableStateFlow<DataSource<List<PeriodicTask>>>(DataSource.loading(emptyList()))
    val periodicTaskFlow : StateFlow<DataSource<List<PeriodicTask>>> = _periodicTaskFlow

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
        startFetchTemperature(1)
        fetchPeriodicTasks()
        Logger.d("emit - refreshState - success")
    }

    private fun readState() {
        viewModelScope.launch {
            _tankControlStateFlow.emit(DataSource.loading(TankState()))
            combine(
                tankDataSource.readInWaterState(),
                tankDataSource.readOutWaterState(),
                tankDataSource.readLightBrightness(),
                tankDataSource.readHeaterState(),
            ) { inWaterState, outWaterState, brightness, heaterState ->
                TankState(
                    inWaterValveState = inWaterState,
                    outWaterValveState = outWaterState,
                    brightness = brightness.toInt(),
                    heaterState = heaterState
                )
            }.collect {
                Logger.d("emit - tankState $it")
                _tankControlStateFlow.emit(DataSource.success(it))
            }
        }
    }

    fun startFetchTemperature(days: Int) {
        viewModelScope.launch {
            _temperatureFlow.emit(DataSource.loading(emptyList()))
            tankDataSource.readDBTemperature(days).collect {
                Logger.d("emit - startFetchTemperature")
                _temperatureFlow.emit(DataSource.success(it))
            }
        }
    }

    private fun fetchPeriodicTasks() {
        viewModelScope.launch {
            _periodicTaskFlow.emit(DataSource.loading(emptyList()))
            tankDataSource.fetchPeriodicTasks().collect {
                Logger.d("emit - fetchPeriodicTasks")
                _periodicTaskFlow.emit(DataSource.success(it))
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
    val outWaterValveState: Boolean = false,
    val inWaterValveState: Boolean = false,
    val heaterState: Boolean = false,
    val brightness: Int = 0,
)
