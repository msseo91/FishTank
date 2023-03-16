package com.marine.fishtank.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marine.fishtank.api.TankDataSource
import com.marine.fishtank.model.DataSource
import com.marine.fishtank.model.DeviceState
import com.marine.fishtank.model.PeriodicTask
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ControlViewModel @Inject constructor(
    private val tankDataSource: TankDataSource
) : ViewModel() {
    private val _tankControlStateFlow = MutableStateFlow(DataSource.loading(DeviceState()))
    val tankControlStateFlow: StateFlow<DataSource<DeviceState>> = _tankControlStateFlow

    private val _periodicTasks = MutableStateFlow(emptyList<PeriodicTask>())
    val periodicTasks: StateFlow<List<PeriodicTask>> = _periodicTasks

    fun readPeriodicTasks() {
        viewModelScope.launch {
            tankDataSource.fetchPeriodicTasks().collect {
                _periodicTasks.emit(it)
            }
        }
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

    fun fetchTemperature(days: Int) = tankDataSource.readDBTemperature(days)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

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
        tankDataSource.addPeriodicTask(periodicTask).collect { readPeriodicTasks() }
    }

    fun deletePeriodicTask(id: Int) = viewModelScope.launch {
        tankDataSource.deletePeriodicTask(id).collect { readPeriodicTasks() }
    }
}

