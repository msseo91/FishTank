package com.marineseo.fishtank.fishwebserver.service

import com.marineseo.fishtank.fishwebserver.mapper.DatabaseMapper
import com.marineseo.fishtank.fishwebserver.model.Temperature
import com.marineseo.fishtank.fishwebserver.util.TimeUtils
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.event.*
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

private const val TEMPERATURE_INTERVAL = TimeUtils.MILS_MINUTE * 5
private const val SERVICE_ID = Integer.MAX_VALUE

private const val TAG = "TemperatureService"

@Service
class TemperatureService(
    private val arduinoService: ArduinoService,
    private val mapper: DatabaseMapper
): ApplicationListener<ApplicationContextEvent> {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val scope = CoroutineScope(Dispatchers.IO)
    private var isRunning = false

    override fun onApplicationEvent(event: ApplicationContextEvent) {
        logger.warn("onApplicationEvent - $event")

        when(event) {
            is ContextStartedEvent -> {
                init()
            }
            is ContextClosedEvent, is ContextStoppedEvent -> {
                isRunning = false
            }
            is ContextRefreshedEvent -> {
                // Refresh.
                isRunning = false
                init()
            }
        }
    }

    private fun init() {
        logger.info("Init - Application start!")
        readTemperatureForever()
    }

    /**
     * 지속적으로 Temperature 을 읽어 DB 에 저장한다.
     */
    private fun readTemperatureForever() {
        if(isRunning) {
            logger.error("Already running!")
            return
        }

        isRunning = true

        scope.launch {
            while(isRunning) {
                val temperature = arduinoService.getTemperature()
                if(temperature > 0) {
                    mapper.insertTemperature(
                        Temperature(
                            temperature = temperature
                        )
                    )
                }
                delay(TEMPERATURE_INTERVAL)
            }
        }
    }

    fun readTemperature(days: Int): List<Temperature> {
        val daysInMils = TimeUtils.MILS_DAY * days
        val from = Timestamp(System.currentTimeMillis() - daysInMils)
        val until = Timestamp(System.currentTimeMillis())
        val temperatures = mapper.fetchTemperature(from, until)

        // for logging
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        logger.info(
            "Fetching from ${formatter.format(from)} until ${formatter.format(until)} tempSize=${temperatures.size}"
        )

        return temperatures
    }

}