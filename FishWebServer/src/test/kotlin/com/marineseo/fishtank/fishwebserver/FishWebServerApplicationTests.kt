package com.marineseo.fishtank.fishwebserver

import com.marineseo.fishtank.fishwebserver.model.*
import com.marineseo.fishtank.fishwebserver.service.ArduinoService
import com.marineseo.fishtank.fishwebserver.util.runCommand
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class FishWebServerApplicationTests {
    private val packet = FishPacket(
        clientId = 1,
        id = 2,
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
        for(i in 0..Short.MAX_VALUE) {
            testPacketConversion(FishPacket(
                id = i,
                clientId = i,
                opCode = (i+100).toShort(),
                pin = i.toShort(),
                pinMode = i.toShort(),
                data = i * 0.75f
            ))
        }
    }

    private fun testPacketConversion(packet: FishPacket) {
        val buffer = packet.toRawPacket()
        val aPacket = buffer.toPacket()

        println("origin=$packet")
        println("decoded=$aPacket")
        Assertions.assertEquals(packet, aPacket)
        Assertions.assertTrue(aPacket.isValidate())
        Assertions.assertTrue(packet.isValidate())
    }

}
