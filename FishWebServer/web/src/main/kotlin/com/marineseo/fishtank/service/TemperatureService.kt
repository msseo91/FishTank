package com.marineseo.fishtank.service

import com.marineseo.fishtank.mapper.DatabaseMapper
import com.marineseo.fishtank.model.Temperature
import com.marineseo.fishtank.util.TimeUtils
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.event.*
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

private const val TEMPERATURE_INTERVAL = TimeUtils.MILS_SECOND * 30
private const val TEMPERATURE_SAVE_INTERVAL = 10
private const val SERVICE_ID = Integer.MAX_VALUE

private const val TAG = "TemperatureService"

private const val TARGET_TEMPERATURE = 26

@Service
class TemperatureService(
    private val mapper: DatabaseMapper,
    private val raspberryService: RaspberryService
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
            var times = 0
            while(isRunning) {
                times++
                val temperature = raspberryService.getTemperatureInTank()
                if(temperature > 0 && (times % TEMPERATURE_SAVE_INTERVAL == 0)) {
                    mapper.insertTemperature(Temperature(temperature = temperature))
                }

                // Turn on/off heater based on temperature.
                raspberryService.enableHeater(temperature < TARGET_TEMPERATURE)

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