package com.marineseo.fishtank.device

import org.slf4j.LoggerFactory
import java.io.File

private const val W1_DEVICE_PATH = "/sys/bus/w1/devices"
private const val W1_FILE_NAME = "w1_slave"

class Ds18b20 {
    private val logger = LoggerFactory.getLogger(Ds18b20::class.java)

    private val sysFile = File(W1_DEVICE_PATH).listFiles()?.find {
        it.isDirectory && it.name.startsWith("28-")
    }?.listFiles()?.find { it.name == W1_FILE_NAME }

    fun readTemperature(): Double = runCatching {
        sysFile?.readText()?.substringAfterLast("t=")?.trim()?.toInt()?.let { it / 1000.0 } ?: -1.0
    }.onFailure { logger.error(it.toString()) }.let { it.getOrNull() ?: -1.0 }
}