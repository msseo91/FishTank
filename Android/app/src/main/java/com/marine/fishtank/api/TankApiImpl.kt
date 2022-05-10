package com.marine.fishtank.api

import com.marine.fishtank.model.TankData
import com.marine.fishtank.model.ServerPacket
import com.marine.fishtank.model.toJson

class TankApiImpl(): TankApi, MessageListener {
    private val client = Client()
    private var listener: TankApi.OnServerPacketListener? = null

    override fun connect(url: String, port: Int): Boolean {
        val connectResult = client.connect(url, port)
        if(connectResult) {
            client.registerListener(this)
        }
        return connectResult
    }

    override fun sendCommand(packet: ServerPacket): List<TankData> {
        // TODO - impl send packet and get response
        client.send(packet.toJson())

        return emptyList()
    }

    override fun startListen() {
    }

    override fun stopListen() {
        // TODO - impl stop listen form socket
    }

    override fun disConnect() {
        client.unRegisterListener()
        client.disConnect()
    }

    override fun registerServerPacketListener(listener: TankApi.OnServerPacketListener) {
        this.listener = listener
        client.startListen()
    }

    override fun unRegisterServerPacketListener() {
        this.listener = null
    }

    /**
     * Called by Client
     */
    override fun onServerMessage(packet: ServerPacket) {
        listener?.onServerPacket(packet)
    }
}