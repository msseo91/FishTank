package com.marine.fishtank.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marine.fishtank.api.TankDataSource
import com.marine.fishtank.model.DataSource
import com.marine.fishtank.model.DeviceState
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
    private val _tankControlStateFlow = MutableStateFlow(DataSource.loading(DeviceState()))
    val tankControlStateFlow: StateFlow<DataSource<DeviceState>> = _tankControlStateFlow

    private val _temperatureFlow = MutableStateFlow<DataSource<List<Temperature>>>(DataSource.loading(emptyList()))
    val temperatureFlow : StateFlow<DataSource<List<Temperature>>> = _temperatureFlow

    private val _periodicTaskFlow = MutableStateFlow<DataSource<List<PeriodicTask>>>(DataSource.loading(emptyList()))
    val periodicTaskFlow : StateFlow<DataSource<List<PeriodicTask>>> = _periodicTaskFlow

    init {
        Logger.d("init - ControlViewModel")
    }

    fun readDeviceState() {
        viewModelScope.launch {
            // emit last TankState with 'Loading' state.
            _tankControlStateFlow.emit(DataSource.loading(_tankControlStateFlow.value.data))
            tankDataSource.readAllState().collect {
                Logger.d("emit - readDeviceState")
                _tankControlStateFlow.emit(DataSource.success(it))
            }
        }
    }

    fun fetchTemperature(days: Int) {
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
        tankDataSource.enableOutWater(enable).collect { readDeviceState() }
    }

    fun enableOutWater2(enable: Boolean) = viewModelScope.launch {
        tankDataSource.enableOutWater2(enable).collect { readDeviceState() }
    }

    fun enableInWater(enable: Boolean) = viewModelScope.launch {
        tankDataSource.enableInWater(enable).collect { readDeviceState() }
    }

    fun enableHeater(enable: Boolean) = viewModelScope.launch {
        tankDataSource.enableHeater(enable).collect { readDeviceState() }
    }

    fun changeLightBrightness(brightness: Float) = viewModelScope.launch {
        tankDataSource.changeLightBrightness(brightness).collect()
    }

    fun addPeriodicTask(periodicTask: PeriodicTask) = viewModelScope.launch {
        tankDataSource.addPeriodicTask(periodicTask).collect { fetchPeriodicTasks() }
    }

    fun deletePeriodicTask(id: Int) = viewModelScope.launch {
        tankDataSource.deletePeriodicTask(id).collect { fetchPeriodicTasks() }
    }
}

