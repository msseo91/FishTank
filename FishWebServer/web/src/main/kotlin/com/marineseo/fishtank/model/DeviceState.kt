package com.marineseo.fishtank.model

data class DeviceState(
    val isOutletValve1Enabled: Boolean = false,
    val isOutletValve2Enabled: Boolean = false,
    val isInletValveEnabled: Boolean = false,
    val isHeaterEnabled: Boolean = false,
    val isLightOn: Boolean = false,
)
