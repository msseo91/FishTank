package com.marine.fishtank.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.marine.fishtank.ConnectionSetting
import com.marine.fishtank.DEFAULT_CONNECTION_SETTING
import com.marine.fishtank.SettingsRepository
import com.marine.fishtank.api.OnServerPacketListener
import com.marine.fishtank.api.TankApi
import com.marine.fishtank.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "FishTankViewModel"

// 어항 물 용량
private const val TANK_WATER_VOLUME = 100

data class UiState(
    var outWaterValveState: Boolean = false,
    var inWaterValveState: Boolean = false,
    var lightState: Boolean = false,
    var pumpState: Boolean = false,
    var heaterState: Boolean = false,
    var purifierState: Boolean = false,

    var temperature: Double = 0.0,
    var temperatureDays: Float = 0f,

    var resultText: String = "",

    var connectionSetting: ConnectionSetting = DEFAULT_CONNECTION_SETTING,
    var serverUrl: String = ""
)

sealed class UiEvent(
    val value: Boolean = false,
    val intValue: Int = 0
) {
    class OutWaterEvent(enable: Boolean) : UiEvent(enable)
    class InWaterEvent(enable: Boolean) : UiEvent(enable)
    class LightEvent(enable: Boolean) : UiEvent(enable)
    class PumpEvent(enable: Boolean) : UiEvent(enable)
    class HeaterEvent(enable: Boolean) : UiEvent(enable)
    class PurifierEvent(enable: Boolean) : UiEvent(enable)
    class LedEvent(enable: Boolean) : UiEvent(enable)

    class ReplaceWater(val ratio: Int) : UiEvent()
    class OnChangeTemperatureRange(count: Int) : UiEvent(intValue = count)

    class OnPlayButtonClick : UiEvent()
    class SettingChange(val connectionSetting: ConnectionSetting) : UiEvent()
}

class FishTankViewModel(application: Application) : AndroidViewModel(application) {
    val temperatureLiveData = MutableLiveData<List<Temperature>>()
    val initializeLiveData = MutableLiveData<Boolean>()
    private val settingsRepository = SettingsRepository.getInstance(context = application)

    val lastConnectionSetting: ConnectionSetting?
        get() = _lastConnectionSetting
    private var _lastConnectionSetting: ConnectionSetting? = null

    private val tankApi: TankApi = TankApi

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState>
        get() = _uiState

    private val packetListener = object : OnServerPacketListener {
        override fun onServerPacket(packet: ServerPacket) {
            // packet sent by server.
            Log.d(TAG, "onServerPacket=$packet")
            when (packet.opCode) {
                SERVER_OP_READ_TEMPERATURE -> {
                    // only one temperature!
                    if(packet.temperatureList.isNotEmpty()) {
                        val list = mutableListOf<Temperature>()
                        list.add(packet.temperatureList[0])
                        temperatureLiveData.postValue(
                            list
                        )
                    }
                }
                SERVER_OP_DB_TEMPERATURE -> {
                    // List of temperature!
                    temperatureLiveData.postValue(packet.temperatureList)
                }
                SERVER_OP_READ_IN_WATER -> {
                    _uiState.postValue(_uiState.value?.copy(inWaterValveState = packet.pinState))
                }
                SERVER_OP_READ_OUT_WATER -> {
                    _uiState.postValue(_uiState.value?.copy(outWaterValveState = packet.pinState))
                }
            }
        }
    }

    fun init() {
        // Post first empty value to copy later.
        _uiState.postValue(
            UiState()
        )

        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.settingFlow.collect { connectionSetting ->
                if(_lastConnectionSetting != connectionSetting) {
                    Log.d(TAG, "ConnectionSetting updated! $connectionSetting")
                    _uiState.value?.let {
                        _uiState.postValue(it.copy(
                            connectionSetting = connectionSetting
                        ))
                    }

                    if(_lastConnectionSetting?.serverUrl != connectionSetting.serverUrl
                        || _lastConnectionSetting?.serverPort != connectionSetting.serverPort) {
                        // Server url is updated or it is first time!
                        val connectResult = tankApi.connect(connectionSetting.serverUrl, connectionSetting.serverPort)
                        initializeLiveData.postValue(connectResult)
                        tankApi.registerServerPacketListener(packetListener)
                    }

                    if(_lastConnectionSetting?.rtspUrl != connectionSetting.rtspUrl) {
                        // rtsp url is updated.
                    }

                    _lastConnectionSetting = connectionSetting
                }
            }
        }
    }

    fun readState() {
        viewModelScope.launch(Dispatchers.IO) {
            tankApi.sendCommand(ServerPacket(clientId = AppId.MY_ID, opCode = SERVER_OP_READ_IN_WATER))
            tankApi.sendCommand(ServerPacket(clientId = AppId.MY_ID, opCode = SERVER_OP_READ_OUT_WATER))
        }
    }

    fun startFetchTemperature(days: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            tankApi.sendCommand(ServerPacket(clientId = AppId.MY_ID, opCode = SERVER_OP_DB_TEMPERATURE, data = days))
            val daysInFl = days.toFloat()
            Log.d(TAG, "DaysInFl=$daysInFl")
        }
    }

    private fun replaceWater(ratio: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            tankApi.sendCommand(
                ServerPacket(
                    clientId = AppId.MY_ID,
                    opCode = SERVER_OP_WATER_REPLACE,
                    data = ratio
                )
            )
        }
    }

    private fun enableOutWaterValve(open: Boolean) {
        tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_OUT_WATER, data = if (open) 1 else 0))
    }

    private fun enableInWaterValve(open: Boolean) {
        tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_IN_WATER, data = if (open) 1 else 0))
    }

    private fun enablePump(run: Boolean) {
        tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_WATER_PUMP, data = if (run) 1 else 0))
    }

    private fun enableLight(enable: Boolean) {
        tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_LIGHT, data = if (enable) 1 else 0))
    }

    private fun enableHeater(enable: Boolean) {
        tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_HEATER, data = if (enable) 1 else 0))
    }

    private fun enablePurifier(enable: Boolean) {
        tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_PURIFIER_1, data = if (enable) 1 else 0))
        tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_PURIFIER_2, data = if (enable) 1 else 0))
    }

    private fun enableBoardLed(enable: Boolean) {
        tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_MEGA_LED, data = if (enable) 1 else 0))
    }

    fun uiEvent(uiEvent: UiEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            when (uiEvent) {
                is UiEvent.OutWaterEvent -> {
                    _uiState.postValue(
                        _uiState.value?.copy(
                            resultText = "${if (uiEvent.value) "Open" else "Close"} Out-Water valve!",
                            outWaterValveState = uiEvent.value,
                        )
                    )
                    enableOutWaterValve(uiEvent.value)
                }
                is UiEvent.InWaterEvent -> {
                    _uiState.postValue(
                        _uiState.value?.copy(
                            inWaterValveState = uiEvent.value,
                            resultText = "${if (uiEvent.value) "Open" else "Close"} In-Water valve!"
                        )
                    )
                    enableInWaterValve(uiEvent.value)
                }
                is UiEvent.LightEvent -> {
                    _uiState.postValue(
                        _uiState.value?.copy(
                            lightState = uiEvent.value,
                            resultText = "Light ${if (uiEvent.value) "On" else "Off"} "
                        )
                    )
                    enableLight(uiEvent.value)
                }
                is UiEvent.PumpEvent -> {
                    _uiState.postValue(
                        _uiState.value?.copy(
                            pumpState = uiEvent.value,
                            resultText = "Pump ${if (uiEvent.value) "On" else "Off"} "
                        )
                    )
                    enablePump(uiEvent.value)
                }
                is UiEvent.HeaterEvent -> {
                    _uiState.postValue(
                        _uiState.value?.copy(
                            heaterState = uiEvent.value,
                            resultText = "Heater ${if (uiEvent.value) "On" else "Off"} "
                        )
                    )
                    enableHeater(uiEvent.value)
                }
                is UiEvent.PurifierEvent -> {
                    _uiState.postValue(
                        _uiState.value?.copy(
                            purifierState = uiEvent.value,
                            resultText = "Purifier ${if (uiEvent.value) "On" else "Off"} "
                        )
                    )
                    enablePurifier(uiEvent.value)
                }
                is UiEvent.ReplaceWater -> {
                    _uiState.postValue(
                        _uiState.value?.copy(
                            resultText = "Start change-water"
                        )
                    )
                    replaceWater(uiEvent.ratio)
                }
                is UiEvent.LedEvent -> {
                    _uiState.postValue(
                        _uiState.value?.copy(
                            resultText = "${if (uiEvent.value) "On" else "Off"} Board LED"
                        )
                    )
                    enableBoardLed(uiEvent.value)
                }
                is UiEvent.OnChangeTemperatureRange -> {
                    startFetchTemperature(uiEvent.intValue)
                }
                is UiEvent.OnPlayButtonClick -> {

                }
                is UiEvent.SettingChange -> {
                    val setting = uiEvent.connectionSetting
                    settingsRepository.saveServerUrl(setting.serverUrl)
                    settingsRepository.saveServerPort(setting.serverPort)
                    settingsRepository.saveRtspUrl(setting.rtspUrl)
                }
            }
        }
    }

}