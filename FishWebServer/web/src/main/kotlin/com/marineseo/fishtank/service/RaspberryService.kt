package com.marineseo.fishtank.service

import com.marineseo.fishtank.device.FishTankDevice
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class RaspberryService(
    private val fishTankDevice: FishTankDevice
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun enableOutWaterValve(open: Boolean) {
        logger.debug("enable output water valve1: $open")
        if (open) fishTankDevice.outletValve1.low()
        else fishTankDevice.outletValve1.high()
    }

    fun enableOutWaterValve2(open: Boolean) {
        logger.debug("enable output water valve2: $open")
        if (open) fishTankDevice.outletValve2.low()
        else fishTankDevice.outletValve2.high()
    }

    fun enableInWaterValve(open: Boolean) {
        logger.debug("enable input water valve: $open")
        if (open) fishTankDevice.inletValve.high()
        else fishTankDevice.inletValve.low()
    }

    fun isInWaterValveOpen(): Boolean {
        val isOpen = fishTankDevice.inletValve.isHigh
        logger.debug("isInWaterValveOpen=$isOpen")
        return isOpen
    }

    fun isOutWaterValveOpen(): Boolean {
        val isOpen = fishTankDevice.outletValve1.isLow
        logger.debug("isOutWaterValve1Open=$isOpen")
        return isOpen
    }

    fun isOutWaterValve2Open(): Boolean {
        val isOpen = fishTankDevice.outletValve2.isLow
        logger.debug("isOutWaterValve2Open=$isOpen")
        return isOpen
    }

/*    fun readBrightness(): Float {
        val dutyCycle = fishTankDevice.light.dutyCycle()
        logger.debug("read Brightness=$dutyCycle")
        return dutyCycle / 100f
    }

    fun adjustBrightness(value: Float) {
        logger.debug("adjustBrightness=$value")
        fishTankDevice.light.on(value * 100)
    }*/

    fun enableHeater(enable: Boolean) {
        logger.debug("enableHeater=$enable")
        if (enable) fishTankDevice.heater.low()
        else fishTankDevice.heater.high()
    }

    fun isHeaterOn(): Boolean {
        val isOn = fishTankDevice.heater.isLow
        logger.debug("isHeaterOn=$isOn")
        return isOn
    }

    fun enableLight(enable: Boolean) {
        logger.debug("enablePump=$enable")
        if(enable) fishTankDevice.light.low()
        else fishTankDevice.light.high()
    }

    fun isLightOn(): Boolean {
        val isOn = fishTankDevice.light.isLow
        logger.debug("isPumpOn=$isOn")
        return isOn
    }

    fun getTemperatureInTank(): Float {
        val temperature = fishTankDevice.temperature.readTemperature().toFloat()
        logger.debug("getTemperatureInTank=$temperature")
        return temperature
    }

    fun getTemperatureInDevice(): Float {
        // TODO - IMPL
        return 0f
    }
}