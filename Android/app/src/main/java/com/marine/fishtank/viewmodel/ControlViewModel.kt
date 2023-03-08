package com.marine.fishtank.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marine.fishtank.api.TankDataSource
import com.marine.fishtank.model.DataSource
import com.marine.fishtank.model.PeriodicTask
import com.marine.fishtank.model.Temperature
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ControlViewModel @Inject constructor(
    private val tankDataSource: TankDataSource
) : ViewModel() {
    private val _tankControlStateFlow = MutableStateFlow<DataSource<TankState>>(DataSource.loading(TankState()))
    val tankControlStateFlow: StateFlow<DataSource<TankState>> = _tankControlStateFlow

    private val _temperatureFlow = MutableStateFlow<DataSource<List<Temperature>>>(DataSource.loading(emptyList()))
    val temperatureFlow : StateFlow<DataSource<List<Temperature>>> = _temperatureFlow

    private val _periodicTaskFlow = MutableStateFlow<DataSource<List<PeriodicTask>>>(DataSource.loading(emptyList()))
    val periodicTaskFlow : StateFlow<DataSource<List<PeriodicTask>>> = _periodicTaskFlow

    init {
        Logger.d("init - ControlViewModel")
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
            // emit last TankState with 'Loading' state.
            _tankControlStateFlow.emit(DataSource.loading(_tankControlStateFlow.value.data))
            combine(
                tankDataSource.readInWaterState(),
                tankDataSource.readOutWaterState(),
                tankDataSource.readLightBrightness(),
                tankDataSource.readHeaterState(),
                tankDataSource.readOutWaterState2()
            ) { inWaterState, outWaterState, brightness, heaterState, outWaterState2 ->
                TankState(
                    inWaterValveState = inWaterState,
                    outWaterValveState = outWaterState,
                    outWaterValve2State = outWaterState2,
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

    fun enableOutWater2(enable: Boolean) = viewModelScope.launch {
        tankDataSource.enableOutWater2(enable).collect { readState() }
    }

    fun enableInWater(enable: Boolean) = viewModelScope.launch {
        tankDataSource.enableInWater(enable).collect { readState() }
    }

    fun enableHeater(enable: Boolean) = viewModelScope.launch {
        tankDataSource.enableHeater(enable).collect { readState() }
    }

    fun changeLightBrightness(brightness: Int) = viewModelScope.launch {
        tankDataSource.changeLightBrightness(brightness * 0.01f).collect()
    }

    fun addPeriodicTask(periodicTask: PeriodicTask) = viewModelScope.launch {
        tankDataSource.addPeriodicTask(periodicTask).collect { fetchPeriodicTasks() }
    }

    fun deletePeriodicTask(periodicTask: PeriodicTask) = viewModelScope.launch {
        tankDataSource.deletePeriodicTask(periodicTask).collect { fetchPeriodicTasks() }
    }
}

data class TankState(
    val outWaterValveState: Boolean = false,
    val outWaterValve2State: Boolean = false,
    val inWaterValveState: Boolean = false,
    val heaterState: Boolean = false,
    val brightness: Int = 0,
)
