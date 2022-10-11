package com.marineseo.fishtank.fishwebserver

import com.marineseo.fishtank.fishwebserver.model.*
import com.marineseo.fishtank.fishwebserver.service.ArduinoService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class FishWebServerApplicationTests {
    private val packet = FishPacket(
        id = 2,
        clientId = 1,
        opCode = 200,
        pin = 10,
        pinMode = 1,
        data = 3.5f
    )

    @Test
    fun generalTest() {
        val service = ArduinoService()
        service.init()

        service.enableInWaterValve(true)
        service.enableOutWaterValve(true)
    }

    @Test
    fun crcTest() {
        println("CRC=${packet.makeCrc()}")
    }

    @Test
    fun serializeTest() {
        testPacketConversion(packet)
    }

    @Test
    fun deSerializeTest() {
        val testRawPacket = arrayOf(
            0x02, 0xeb, 0x05, 0x00, 0x00, 0xb7, 0x0b, 0x00, 0x00, 0x03, 0x00, 0x32, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x22,
            0xb7, 0x03,
        )

        val packet = testRawPacket.map { it.toByte() }.toByteArray().toPacket()
        println("$packet VALID=${packet.isValidate()}")
    }

    private fun testPacketConversion(packet: FishPacket) {
        val buffer = packet.toRawPacket()
        val aPacket = buffer.toPacket()

        // Print buffer
        println("BUFFER=${buffer.toHex2()}")

        println("origin=$packet")
        println("decoded=$aPacket")
        Assertions.assertEquals(packet, aPacket)
        Assertions.assertTrue(aPacket.isValidate())
        Assertions.assertTrue(packet.isValidate())
    }

    fun ByteArray.toHex2(): String = asUByteArray().joinToString(", ") {
        "0x" + it.toString(radix = 16).padStart(2, '0')
    }
}
