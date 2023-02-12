package com.marineseo.fishtank.fishwebserver.model

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and
import kotlin.experimental.or

const val OP_GET_TEMPERATURE: Short = 1000
const val OP_PIN_IO: Short = 1001
const val OP_READ_DIGIT_PIN: Short = 1002
const val OP_INPUT_ANALOG_PIN: Short = 1003
const val OP_READ_ANALOG_PIN: Short = 1004

const val MAGIC: Short = 31256
const val PACKET_SIZE = 22

private var autoIncId: Int = 0

const val STX: Byte = 0x02
const val ETX: Byte = 0x03
const val DLE: Byte = 0x10

data class FishPacket(
    val id: Int = ++autoIncId,
    val clientId: Int = 0,
    val opCode: Short = 0,
    val pin: Short = 0,
    val pinMode: Short = 0,
    val data: Float = 0f,
    var crc: Short = 0
)

fun FishPacket.makeCrc(): Short {
    return ((id shl 12).toShort() and 0xF000.toShort()) or
            ((clientId shl 8).toShort() and 0x0F00) or
            ((pin.toInt() shl 4).toShort() and 0x00F0) or
            (pinMode and 0x000F)
}

fun FishPacket.isValidate(): Boolean {
    return crc == makeCrc()
}

fun FishPacket.toRawPacket(): ByteArray {
    crc = makeCrc()

    val buffer = ByteBuffer.allocate(PACKET_SIZE).apply {
        order(ByteOrder.LITTLE_ENDIAN)
        put(STX)
        putInt(id)
        putInt(clientId)
        putShort(opCode)
        putShort(pin)
        putShort(pinMode)
        putFloat(data)
        putShort(crc)
        put(ETX)
    }

    return buffer.array()
}

fun ByteArray.toPacket(): FishPacket {
    if(this.size < PACKET_SIZE) throw IllegalArgumentException("Size error. Size must be $PACKET_SIZE at least.")
    if(this.first() != STX) throw IllegalArgumentException("First byte should be $STX but ${this.first()}")
    if(this.last() != ETX) throw IllegalArgumentException("Last byte should be $ETX but ${this.last()}")

    val buffer = ByteBuffer.allocate(this.size).apply {
        put(this@toPacket)
        order(ByteOrder.LITTLE_ENDIAN)
        position(0)
    }

    // Read stx for positioning.
    val stx = buffer.get()

    return FishPacket(
        id = buffer.int,
        clientId = buffer.int,
        opCode = buffer.short,
        pin = buffer.short,
        pinMode = buffer.short,
        data = buffer.float,
        crc = buffer.short
    )
}

fun ByteArray.toHex2(): String = asUByteArray().joinToString(", ") {
    "0x" + it.toString(radix = 16).padStart(2, '0')
}


