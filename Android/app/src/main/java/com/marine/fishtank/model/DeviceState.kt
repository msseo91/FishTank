package com.marine.fishtank.model

data class DeviceState(
    val pumpEnabled: Boolean = false,
    val outletValve1Enabled: Boolean = false,
    val outletValve2Enabled: Boolean = false,
    val inletValveEnabled: Boolean = false,
    val heaterEnabled: Boolean = false,
    val lightBrightness: Float = 0f
)
