package com.marine.fishtank.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marine.fishtank.api.TankDataSource
import com.marine.fishtank.api.TankResult
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
    private val _tankControlStateFlow: MutableStateFlow<TankResult<DeviceState>> = MutableStateFlow(TankResult.Loading())
    val tankControlStateFlow = _tankControlStateFlow.asStateFlow()

    private val _periodicTasks: MutableStateFlow<TankResult<List<PeriodicTask>>> = MutableStateFlow(TankResult.Loading())
    val periodicTaskFlow = _periodicTasks.asStateFlow()

    fun readPeriodicTasks() {
        viewModelScope.launch {
            tankDataSource.fetchPeriodicTasks().collect { _periodicTasks.emit(it) }
        }
    }

    fun readDeviceState() {
        viewModelScope.launch {
            tankDataSource.readAllState().collect {
                Logger.d("emit - readDeviceState $it")
                _tankControlStateFlow.emit(it)
            }
        }
    }

    fun fetchTemperature(days: Int) = tankDataSource.readDBTemperature(days)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = TankResult.Loading()
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

    fun enableLight(enable: Boolean) = viewModelScope.launch {
        tankDataSource.enableLight(enable).collect { readDeviceState() }
    }

    fun enableCo2(enable: Boolean) = viewModelScope.launch {
        tankDataSource.enableCo2(enable).collect { readDeviceState() }
    }

    fun addPeriodicTask(periodicTask: PeriodicTask) = viewModelScope.launch {
        tankDataSource.addPeriodicTask(periodicTask).collect { readPeriodicTasks() }
    }

    fun deletePeriodicTask(id: Int) = viewModelScope.launch {
        tankDataSource.deletePeriodicTask(id).collect { readPeriodicTasks() }
    }
}

