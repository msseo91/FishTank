package com.marineseo.fishtank.device

import com.pi4j.Pi4J
import com.pi4j.io.gpio.digital.DigitalOutput
import com.pi4j.io.gpio.digital.DigitalState
import com.pi4j.io.pwm.Pwm
import com.pi4j.io.pwm.PwmType
import org.springframework.stereotype.Component

private const val PROVIDER_INPUT = "pigpio-digital-input"
private const val PROVIDER_OUT = "pigpio-digital-output"
private const val PROVIDER_PWM = "pigpio-pwm"

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

    val heater: DigitalOutput = pi4j.create(
        DigitalOutput.newConfigBuilder(pi4j)
            .address(Pin.HEATER.bsmPin)
            .initial(DigitalState.HIGH)
            .provider(PROVIDER_OUT)
            .build()
    )

    val light: Pwm = pi4j.create(
        Pwm.newConfigBuilder(pi4j)
            .address(Pin.LIGHT.bsmPin)
            .pwmType(PwmType.HARDWARE)
            .provider(PROVIDER_PWM)
            .shutdown(0)
            .build()
    ).also { it.on(0) }

    val temperature = Ds18b20()

}