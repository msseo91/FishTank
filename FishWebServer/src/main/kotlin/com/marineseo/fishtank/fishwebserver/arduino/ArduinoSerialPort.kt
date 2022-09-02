package com.marineseo.fishtank.fishwebserver.arduino

import com.marineseo.fishtank.fishwebserver.model.*
import jssc.SerialPort
import jssc.SerialPortTimeoutException
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.lang.StringBuilder
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val MAX_READ_ATTEMPT = 500
private const val READ_INTERVAL = 10
private const val READ_TIMEOUT = MAX_READ_ATTEMPT * READ_INTERVAL // ms

class ArduinoSerialPort(portName: String): SerialPort(portName) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    private fun readByte(): Byte? {
        return readBytes(1)?.get(0)
    }

    fun readP(): FishPacket? {
        var readStx = false
        var readEtx = false

        val buffer = ByteBuffer.allocate(PACKET_SIZE * 2)

        while(true) {
            readByte()?.let { b->
                if(!readStx) {
                    // First byte.
                    if(b == STX) {
                        readStx = true
                    } else {
                        logger.error("First byte must be STX($STX) BUT $b")
                        return null
                    }
                }

                buffer.put(b)

            }

        }
    }

    fun readPacket(): FishPacket? {
        try {

            // Print bytes for debugging
            val stringBuilder = StringBuilder()
            for(b in bytes) {
                stringBuilder.append(String.format("%X ", b))
            }
            //logger.info("Raw Packet=${stringBuilder}")

            val byteBuffer = ByteBuffer.allocate(PACKET_SIZE).apply {
                order(ByteOrder.LITTLE_ENDIAN)
            }
            byteBuffer.put(bytes)
            byteBuffer.position(0)
            // Read magic
            val magic = byteBuffer.short
            if(magic != MAGIC) {
                logger.error("Unexpected magic! $magic")
                return null
            }

            val packet = FishPacket(
                id = byteBuffer.int,
                clientId = byteBuffer.int,
                opCode = byteBuffer.short,
                pin = byteBuffer.short,
                pinMode = byteBuffer.short,
                data = byteBuffer.float
            )
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
        logger.info("Write $packet")
        return writeBytes(packet.toRawPacket())
    }
}