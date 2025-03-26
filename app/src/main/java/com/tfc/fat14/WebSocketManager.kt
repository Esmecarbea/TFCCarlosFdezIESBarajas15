package com.tfc.fat14

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.util.concurrent.TimeUnit

class WebSocketManager(private val listener: okhttp3.WebSocketListener) { //Se modifica esta linea

    private var webSocket: WebSocket? = null
    private val client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(3, TimeUnit.SECONDS)
        .build()
    private val request: Request = Request.Builder()
        .url("ws://192.168.1.163:81/") // TODO: Cambiar a la IP y puerto de la S3
        .build()

    fun connect() {
        webSocket = client.newWebSocket(request, listener)
        client.dispatcher.executorService.shutdown()
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    fun close() {
        webSocket?.close(1000, "Cierre normal")
    }
}