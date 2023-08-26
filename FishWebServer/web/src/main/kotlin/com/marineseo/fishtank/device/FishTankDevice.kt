package com.marineseo.fishtank.device

import com.pi4j.Pi4J
import com.pi4j.io.gpio.digital.DigitalOutput
import com.pi4j.io.gpio.digital.DigitalState
import com.pi4j.plugin.pigpio.PiGpioPlugin
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextClosedEvent
import org.springframework.stereotype.Component

/**
 * pigpio-digital-input
 * pigpio-digital-output
 * pigpio-pwm
 * pigpio-i2c
 * pigpio-spi
 * pigpio-serial
 */
private const val PROVIDER_INPUT = "pigpio-digital-input"
private const val PROVIDER_OUT = "pigpio-digital-output"
private const val PROVIDER_PWM = "pigpio-pwm"

@Component
class FishTankDevice: ApplicationListener<ContextClosedEvent> {
    private val logger = LoggerFactory.getLogger(FishTankDevice::class.java)
    private val pi4j = Pi4J.newAutoContext()

    val light: DigitalOutput = pi4j.create(
            DigitalOutput.newConfigBuilder(pi4j)
                    .address(Pin.LIGHT.bsmPin)
                    .initial(DigitalState.HIGH)
                    .provider(PiGpioPlugin.DIGITAL_OUTPUT_PROVIDER_ID)
                    .build()
    )

    val outletValve1: DigitalOutput = pi4j.create(
            DigitalOutput.newConfigBuilder(pi4j)
                    .address(Pin.OUTLET_VALVE_1.bsmPin)
                    .initial(DigitalState.HIGH)
                    .provider(PiGpioPlugin.DIGITAL_OUTPUT_PROVIDER_ID)
                    .build()
    )

    val outletValve2: DigitalOutput = pi4j.create(
            DigitalOutput.newConfigBuilder(pi4j)
                    .address(Pin.OUTLET_VALVE_2.bsmPin)
                    .initial(DigitalState.HIGH)
                    .provider(PiGpioPlugin.DIGITAL_OUTPUT_PROVIDER_ID)
                    .build()
    )

    val inletValve: DigitalOutput = pi4j.create(
            DigitalOutput.newConfigBuilder(pi4j)
                    .address(Pin.INLET_VALVE.bsmPin)
                    .initial(DigitalState.HIGH)
                    .provider(PiGpioPlugin.DIGITAL_OUTPUT_PROVIDER_ID)
                    .build()
    )

    val heater: DigitalOutput = pi4j.create(
            DigitalOutput.newConfigBuilder(pi4j)
                    .address(Pin.HEATER.bsmPin)
                    .initial(DigitalState.HIGH)
                    .provider(PiGpioPlugin.DIGITAL_OUTPUT_PROVIDER_ID)
                    .build()
    )

    val co2Valve: DigitalOutput = pi4j.create(
        DigitalOutput.newConfigBuilder(pi4j)
            .address(Pin.CO2_VALVE.bsmPin)
            .initial(DigitalState.HIGH)
            .provider(PiGpioPlugin.DIGITAL_OUTPUT_PROVIDER_ID)
            .build()
    )

    /*val light: Pwm = pi4j.create(
            Pwm.newConfigBuilder(pi4j)
                    .id("light-pwm-bcm")
                    .name("Light pwm")
                    .address(Pin.LIGHT.bsmPin)
                    .pwmType(PwmType.HARDWARE)
                    .provider(PiGpioPlugin.PWM_PROVIDER_ID)
                    .initial(0)
                    .shutdown(0)
                    .dutyCycle(0)
                    .build()
    )*/

    val temperature = Ds18b20()

    override fun onApplicationEvent(event: ContextClosedEvent) {
        logger.warn("Shutdown event received!")

        pi4j.shutdown()
    }
}