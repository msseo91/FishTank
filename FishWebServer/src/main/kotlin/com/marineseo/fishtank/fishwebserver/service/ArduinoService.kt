package com.marineseo.fishtank.fishwebserver.service

import com.marineseo.fishtank.fishwebserver.arduino.ArduinoSerialPort
import com.marineseo.fishtank.fishwebserver.model.*
import com.marineseo.fishtank.fishwebserver.util.DeviceUtils
import jssc.SerialPort
import jssc.SerialPortException
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.event.*
import org.springframework.stereotype.Service

private const val PIN_BOARD_LED: Short = 13

// HW316 Relay - Low Trigger
private const val PIN_RELAY_OUT_WATER: Short = 2
private const val PIN_RELAY_IN_WATER: Short = 3

private const val PIN_LIGHT_BRIGHTNESS: Short = 5

private const val MODE_INPUT: Short = 0x00
private const val MODE_OUTPUT: Short = 0x01
const val HIGH: Short = 0x01
const val LOW: Short = 0x00


private const val TAG = "ArduinoService"

private const val REPAIR_MAX_TRY = 100
private const val RETRY_INTERVAL = 50L
private const val COMMON_CLIENT_ID = 56432

@Service
class ArduinoService : ApplicationListener<ApplicationContextEvent> {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val scope = CoroutineScope(Dispatchers.IO)
    private var port: ArduinoSerialPort? = null
    private var debugPort: SerialPort? = null
    private lateinit var portName: String
    private var runLog = false

    fun init() {
        logger.info("Application start!")

        //connect("COM4")
        val usbDevs = DeviceUtils.getFileList("/dev", "ttyUSB*")
        for (devFile in usbDevs) {
            when (DeviceUtils.getDriver(devFile)) {
                "ch341" -> {
                    logger.info("${devFile.name} id Arduino!")
                    connect(devFile.absolutePath)

                    setRelayHigh(listOf(PIN_RELAY_OUT_WATER, PIN_RELAY_OUT_WATER))
                }
                "ftdi_sio" -> {
                    logger.info("${devFile.name} is debug port")
                    runDebugLog(devFile.absolutePath)
                }
            }
        }

        runBlocking {
            delay(1000L)
        }
    }

    override fun onApplicationEvent(event: ApplicationContextEvent) {
        logger.warn("onApplicationEvent - $event")

        when (event) {
            is ContextStartedEvent -> {
                init()
            }
            is ContextClosedEvent, is ContextStoppedEvent -> {
                runLog = false
                disConnect()
            }
            is ContextRefreshedEvent -> {
                // Refresh
                runLog = false
                disConnect()
                init()
            }
        }
    }

    private fun runDebugLog(port: String) {
        scope.launch {
            try {
                connectDebugPort(port)
                runLog = true
                while (runLog) {
                    debugPort?.readString().let {
                        logger.info("[ARDUINO] $it")
                    }

                    delay(2000L)
                }
            } catch (ex: SerialPortException) {
                logger.error(ex.message)
            }
        }
    }

    private suspend fun connectDebugPort(port: String) {
        try {
            debugPort = SerialPort(port)
            debugPort?.openPort()
            debugPort?.setParams(
                SerialPort.BAUDRATE_57600,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE
            )
            debugPort?.eventsMask = SerialPort.MASK_RXCHAR

            // Wait for some time.
            delay(4000L)
        } catch (ex: SerialPortException) {
            logger.error(ex.message)
        }
    }

    private fun connect(portName: String): Boolean {
        this.portName = portName
        port = ArduinoSerialPort(portName)

        if (port?.isOpened == false) {
            try {
                val openResult = port?.openPort()

                if (openResult != true) {
                    logger.error("Open $portName failed!")
                    return false
                }

                port?.setParams(
                    SerialPort.BAUDRATE_57600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE
                )
                port?.eventsMask = SerialPort.MASK_RXCHAR

                logger.info("Arduino device is connected!")

                runBlocking {
                    delay(3000L)
                    port?.ready = true
                }

                return true
            } catch (ex: SerialPortException) {
                logger.error(ex.toString())
                //throw IOException("Can not connect device($portName)", ex)
            }
        }

        return false
    }

    fun getTemperature(): Float {
        val response = sendAndGetResponse(FishPacket(clientId = COMMON_CLIENT_ID, opCode = OP_GET_TEMPERATURE))
        return response?.data ?: 0f
    }

    fun enableBoardLed(enable: Boolean): Boolean {
        return sendAndGetResponse(
            FishPacket(
                clientId = COMMON_CLIENT_ID,
                opCode = OP_PIN_IO,
                pin = PIN_BOARD_LED,
                pinMode = MODE_OUTPUT,
                data = (if (enable) LOW else HIGH).toFloat()
            )
        ) != null
    }

    fun enableOutWaterValve(open: Boolean): Boolean {
        return sendAndGetResponse(
            FishPacket(
                clientId = COMMON_CLIENT_ID,
                opCode = OP_PIN_IO,
                pin = PIN_RELAY_OUT_WATER,
                pinMode = MODE_OUTPUT,
                data = (if (open) LOW else HIGH).toFloat()
            )
        ) != null
    }

    fun enableInWaterValve(open: Boolean): Boolean {
        // NOTE! in-water solenoid valve is NO(Normally open)
        return sendAndGetResponse(
            FishPacket(
                clientId = COMMON_CLIENT_ID,
                opCode = OP_PIN_IO,
                pin = PIN_RELAY_IN_WATER,
                pinMode = MODE_OUTPUT,
                data = (if (open) HIGH else LOW).toFloat()
            )
        ) != null
    }

    fun isInWaterValveOpen(): Boolean {
        val response = sendAndGetResponse(
            FishPacket(
                clientId = COMMON_CLIENT_ID,
                opCode = OP_READ_DIGIT_PIN,
                pin = PIN_RELAY_IN_WATER,
                pinMode = MODE_INPUT
            )
        )
        return response?.data?.toInt()?.toShort() == HIGH
    }

    fun isOutWaterValveOpen(): Boolean {
        val response = sendAndGetResponse(
            FishPacket(
                clientId = COMMON_CLIENT_ID,
                opCode = OP_READ_DIGIT_PIN,
                pin = PIN_RELAY_OUT_WATER,
                pinMode = MODE_INPUT
            )
        )

        return response?.data?.toInt()?.toShort() == LOW
    }

    fun readBrightness(): Float {
        logger.info("Reading brightness....")

        val response = sendAndGetResponse(
            FishPacket(
                clientId = COMMON_CLIENT_ID,
                opCode = OP_READ_ANALOG_PIN,
                pinMode = MODE_INPUT,
                pin = PIN_LIGHT_BRIGHTNESS
            )
        )

        logger.info("brightness=${response?.data}")
        val brightness = response?.data ?: 0f
        return (brightness / 255) * 100
    }

    fun adjustBrightness(percentage: Float): Boolean {
        val brightness = (255 * percentage).toInt()
        logger.info("Adjusting brightness to $brightness ($percentage)")
        val response = sendAndGetResponse(
            FishPacket(
                clientId = COMMON_CLIENT_ID,
                opCode = OP_INPUT_ANALOG_PIN,
                pin = PIN_LIGHT_BRIGHTNESS,
                pinMode = MODE_OUTPUT,
                data = brightness.toFloat()
            )
        )

        return response != null
    }

    /**
     * Send HIGH to all relay pins to wakeup HW316(Relay)
     */
    private fun setRelayHigh(pins: List<Short>) {
        pins.forEach { pinNumber ->
            sendAndGetResponse(
                FishPacket(
                    clientId = COMMON_CLIENT_ID,
                    opCode = OP_PIN_IO,
                    pin = pinNumber,
                    pinMode = MODE_OUTPUT,
                    data = HIGH.toFloat()
                )
            )
        }
    }

    private fun sendAndGetResponse(packet: FishPacket, depth: Int = 0): FishPacket? {
        val writeResult = port?.writePacket(packet)
        if (writeResult != true) {
            // Fail to write.
            logger.error("Fail to write!")

            if (depth < REPAIR_MAX_TRY) {
                runBlocking { delay(RETRY_INTERVAL) }
                return sendAndGetResponse(packet, depth + 1)
            }
            return null
        }

        val response = port?.readPacket()
        if (response == null) {
            // Fail to read!
            logger.error("Fail to read packet!")

            if (depth < REPAIR_MAX_TRY) {
                return sendAndGetResponse(packet, depth + 1)
            }
        }

        return response
    }

    fun reConnect() {
        runBlocking {
            disConnect()
            delay(2000L)
            connect(portName)
            delay(2000L)
        }
    }

    private fun disConnect() {
        port?.closePort()
    }
}