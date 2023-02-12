package com.marineseo.fishtank;

import com.pi4j.Pi4J;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;

private const val PROVIDER_INPUT = "pigpio-digital-input"
private const val PROVIDER_OUT = "pigpio-digital-output"

fun main(args: Array<String>) {
    println("Hello world!");

    val pi4j = Pi4J.newAutoContext()
    val outletValve1: DigitalOutput = pi4j.create(
        DigitalOutput.newConfigBuilder(pi4j)
            .address(11)
            .initial(DigitalState.HIGH)
            .provider(PROVIDER_OUT)
            .build()
    )

    while(true) {
        println("Toggle!")
        outletValve1.toggle()
        Thread.sleep(1000L)
    }
}
