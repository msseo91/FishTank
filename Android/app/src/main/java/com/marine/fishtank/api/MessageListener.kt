package com.marine.fishtank.api

import com.marine.fishtank.model.ServerPacket

interface MessageListener {
    fun onServerMessage(packet: ServerPacket)
}