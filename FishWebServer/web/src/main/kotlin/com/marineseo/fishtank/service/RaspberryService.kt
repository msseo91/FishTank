package com.marineseo.fishtank.service

import com.marineseo.fishtank.device.FishTankDevice
import org.springframework.stereotype.Service


@Service
class RaspberryService(
    private val fishTankDevice: FishTankDevice
) {
    fun enableOutWaterValve(open: Boolean) {
        if (open) fishTankDevice.outletValve1.low()
        else fishTankDevice.outletValve1.high()
    }

    fun enableOutWaterValve2(open: Boolean) {
        if (open) fishTankDevice.outletValve2.low()
        else fishTankDevice.outletValve2.high()
    }

    fun enableInWaterValve(open: Boolean) {
        if (open) fishTankDevice.inletValve.high()
        else fishTankDevice.inletValve.low()
    }

    fun isInWaterValveOpen(): Boolean = fishTankDevice.inletValve.isHigh

    fun isOutWaterValveOpen() = fishTankDevice.outletValve1.isLow

    fun isOutWaterValve2Open() = fishTankDevice.outletValve2.isLow

    fun readBrightness(): Float = fishTankDevice.light.dutyCycle()

    fun adjustBrightness(value: Float) {
        fishTankDevice.light.on(value * 100)
    }

    fun enableHeater(enable: Boolean) {
        if (enable) fishTankDevice.heater.low()
        else fishTankDevice.heater.high()
    }

    fun isHeaterOn() = fishTankDevice.heater.isLow

    fun enablePump(enable: Boolean) {
        if(enable) fishTankDevice.pump.low()
        else fishTankDevice.pump.high()
    }

    fun isPumpOn() = fishTankDevice.pump.isLow

    fun getTemperatureInTank(): Float {
        // TODO - IMPL
        return 0f
    }

    fun getTemperatureInDevice(): Float {
        // TODO - IMPL
        return 0f
    }
}