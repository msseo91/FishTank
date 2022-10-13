package com.marineseo.fishtank.fishwebserver.arduino

import com.marineseo.fishtank.fishwebserver.model.*
import jssc.SerialPort
import jssc.SerialPortTimeoutException
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val MAX_READ_ATTEMPT = 500
private const val READ_INTERVAL = 10
private const val READ_TIMEOUT = MAX_READ_ATTEMPT * READ_INTERVAL // ms

class ArduinoSerialPort(portName: String): SerialPort(portName) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    var ready = false

    fun readPacket(): FishPacket? {
        if(!ready) return null

        try {
            val bytes = readBytes(PACKET_SIZE, READ_TIMEOUT)
            val byteBuffer = ByteBuffer.allocate(PACKET_SIZE).apply {
                put(bytes)
                order(ByteOrder.LITTLE_ENDIAN)
                position(0)
            }

            val first = byteBuffer.get()
            if(first != STX) {
                logger.error("First byte must be $STX, but $first")
                return null
            }

            val packet = FishPacket(
                id = byteBuffer.int,
                clientId = byteBuffer.int,
                opCode = byteBuffer.short,
                pin = byteBuffer.short,
                pinMode = byteBuffer.short,
                data = byteBuffer.float,
                crc = byteBuffer.short
            )

            if(!packet.isValidate()) {
                logger.error("CRC not matched!")
                return null
            }

            logger.info("Read $packet")
            return packet
        } catch (e: SerialPortTimeoutException) {
            // timeout!
            logger.error(e.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun writePacket(packet: FishPacket): Boolean {
        if(!ready) return false

        val raw = packet.toRawPacket()
        logger.info("Write $packet { ${raw.toHex2()} }")
        return writeBytes(raw)
    }
}