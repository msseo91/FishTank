package com.marineseo.fishtank.fishwebserver.device

import com.marineseo.fishtank.fishwebserver.model.Pin
import com.pi4j.Pi4J
import com.pi4j.io.gpio.digital.DigitalOutput
import com.pi4j.io.gpio.digital.DigitalState
import org.springframework.stereotype.Component

private const val PROVIDER_INPUT = "pigpio-digital-input"
private const val PROVIDER_OUT = "pigpio-digital-output"

@Component
class FishTankDevice {
    private val pi4j = Pi4J.newAutoContext()

    val pump: DigitalOutput = pi4j.create(
        DigitalOutput.newConfigBuilder(pi4j)
            .address(Pin.PUMP.bsmPin)
            .initial(DigitalState.HIGH)
            .provider(PROVIDER_OUT)
            .build()
    )

    val outletValve1: DigitalOutput = pi4j.create(
        DigitalOutput.newConfigBuilder(pi4j)
            .address(Pin.OUTLET_VALVE_1.bsmPin)
            .initial(DigitalState.HIGH)
            .provider(PROVIDER_OUT)
            .build()
    )

    val outletValve2: DigitalOutput = pi4j.create(
        DigitalOutput.newConfigBuilder(pi4j)
            .address(Pin.OUTLET_VALVE_2.bsmPin)
            .initial(DigitalState.HIGH)
            .provider(PROVIDER_OUT)
            .build()
    )

    val inletValve: DigitalOutput = pi4j.create(
        DigitalOutput.newConfigBuilder(pi4j)
            .address(Pin.INLET_VALVE.bsmPin)
            .initial(DigitalState.HIGH)
            .provider(PROVIDER_OUT)
            .build()
    )
}