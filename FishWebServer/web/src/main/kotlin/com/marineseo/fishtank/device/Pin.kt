package com.marineseo.fishtank.device

enum class Pin(val bsmPin: Int) {
    // PIN 22
    LIGHT(25),
    // PIN 23
    OUTLET_VALVE_1(11),
    // PIN 24
    OUTLET_VALVE_2(8),
    // PIN 21
    INLET_VALVE(9),
    // PIN 18
    HEATER(24),
    // PIN 31
    CO2_VALVE(6),

    // PIN 7
    TEMPERATURE_IN_TANK(4),
    // PIN 11
    TEMPERATURE_IN_DEVICE(17)
}