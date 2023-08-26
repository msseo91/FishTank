package com.marine.fishtank.model

data class DeviceState(
    val outletValve1Enabled: Boolean = false,
    val outletValve2Enabled: Boolean = false,
    val inletValveEnabled: Boolean = false,
    val heaterEnabled: Boolean = false,
    val lightOn: Boolean = false,
    val co2ValveOpened: Boolean = false
)
