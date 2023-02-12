package com.marineseo.fishtank.fishwebserver.service

import com.marineseo.fishtank.fishwebserver.device.FishTankDevice
import com.pi4j.io.gpio.digital.DigitalInput
import com.pi4j.io.gpio.digital.PullResistance
import org.springframework.stereotype.Service


@Service
class RaspberryService(
    private val fishTankDevice: FishTankDevice
) {
    fun enableOutWaterValve(open: Boolean) {
        if(open) fishTankDevice.outletValve1.low()
        else fishTankDevice.outletValve1.high()
    }
}