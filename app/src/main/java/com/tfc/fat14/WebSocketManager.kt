package com.tfc.fat14

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import android.util.Log
import java.util.concurrent.TimeUnit

class WebSocketManager(private val listener: WebSocketListener) {
    private var webSocket: WebSocket? = null
    private val client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // Sin timeout para mantener la conexión viva
        .build()
    private val wsUrl = "ws://192.168.1.188:81/ws" // Ajusta esta IP según el ESP32-S3

    fun connect() {
        val request = Request.Builder().url(wsUrl).build()
        webSocket = client.newWebSocket(request, listener)
        Log.d("WebSocketManager", "Conectando a $wsUrl")
    }

    fun sendMessage(message: String) {
        val sent = webSocket?.send(message)
        if (sent == true) {
            Log.d("WebSocketManager", "Mensaje enviado: $message")
        } else {
            Log.e("WebSocketManager", "Fallo al enviar mensaje: $message (WebSocket no conectado)")
        }
    }

    fun close() {
        webSocket?.close(1000, "Cierre normal")
        Log.d("WebSocketManager", "WebSocket cerrado")
    }
}