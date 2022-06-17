package com.marineseo.fishtank.fishwebserver.service

import com.marine.fishtank.server.model.*
import com.marineseo.fishtank.fishwebserver.arduino.ArduinoSerialPort
import jssc.SerialPort
import jssc.SerialPortException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private const val PIN_BOARD_LED: Short = 13

// HW316 Relay - Low Trigger
private const val PIN_RELAY_OUT_WATER: Short = 49
private const val PIN_RELAY_IN_WATER: Short = 48
private const val PIN_RELAY_PUMP: Short = 47
private const val PIN_RELAY_LIGHT: Short = 46
private const val PIN_RELAY_PURIFIER1: Short = 45
private const val PIN_RELAY_PURIFIER2: Short = 44
private const val PIN_RELAY_HEATER: Short = 43

private const val MODE_INPUT: Short = 0x00
private const val MODE_OUTPUT: Short = 0x01
const val HIGH: Short = 0x01
const val LOW: Short = 0x00


private const val TAG = "ArduinoService"

private const val REPAIR_MAX_TRY = 5
private const val COMMON_CLIENT_ID = 56432

@Service
class ArduinoService {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    private var port: ArduinoSerialPort? = null
    private lateinit var portName: String
    fun connect(portName: String): Boolean {
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

    fun enableLight( enable: Boolean): Boolean {
        return sendAndGetResponse(
            FishPacket(
                clientId = COMMON_CLIENT_ID,
                opCode = OP_PIN_IO,
                pin = PIN_RELAY_LIGHT,
                pinMode = MODE_OUTPUT,
                data = (if (enable) LOW else HIGH).toFloat()
            )
        ) != null
    }

    fun enablePurifier(enable: Boolean): Boolean {
        return sendAndGetResponse(
            FishPacket(
                clientId = COMMON_CLIENT_ID,
                opCode = OP_PIN_IO,
                pin = PIN_RELAY_PURIFIER1,
                pinMode = MODE_OUTPUT,
                data = (if (enable) LOW else HIGH).toFloat()
            )
        ) != null
    }

    fun enableHeater( enable: Boolean): Boolean {
        return sendAndGetResponse(
            FishPacket(
                clientId = COMMON_CLIENT_ID,
                opCode = OP_PIN_IO,
                pin = PIN_RELAY_HEATER,
                pinMode = MODE_OUTPUT,
                data = (if (enable) LOW else HIGH).toFloat()
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
        return response?.data?.toInt()?.toShort() == LOW
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

        return response?.data?.toInt()?.toShort() == HIGH
    }

    private fun sendAndGetResponse(packet: FishPacket, depth: Int = 0): FishPacket? {
        val writeResult = port?.writePacket(packet)
        if (writeResult != true) {
            // Fail to write.
            logger.error("Fail to write!")
            repairConnection()

            if (depth < REPAIR_MAX_TRY) {
                sendAndGetResponse(packet, depth + 1)
            }
            return null
        }

        val response = port?.readPacket()
        if (response == null) {
            // Fail to read!
            logger.error("Fail to read packet!")
            repairConnection()

            if (depth < REPAIR_MAX_TRY) {
                sendAndGetResponse(packet, depth + 1)
            }
        }

        return response
    }

    private fun repairConnection() {
        runBlocking {
            disConnect()
            delay(2000L)
            connect(portName)
            delay(2000L)
        }
    }

    fun disConnect() {
        port?.closePort()
    }
}