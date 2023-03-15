package com.marine.fishtank.model

data class DeviceState(
    val isPumpEnabled: Boolean = false,
    val isOutletValve1Enabled: Boolean = false,
    val isOutletValve2Enabled: Boolean = false,
    val isInletValveEnabled: Boolean = false,
    val isHeaterEnabled: Boolean = false,
    val lightBrightness: Float = 0f
)
