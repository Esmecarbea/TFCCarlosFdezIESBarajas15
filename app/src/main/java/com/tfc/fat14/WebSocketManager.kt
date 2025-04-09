package com.tfc.fat14

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import android.util.Log
import java.util.concurrent.TimeUnit

class WebSocketManager {
    private var webSocket: WebSocket? = null
    private val client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()
    private val wsUrl = "ws://192.168.1.188:82/ws"
    private var listener: WebSocketListener? = null
    var lastReceivedMessage: String? = null

    fun connect(listener: WebSocketListener) {
        this.listener = listener
        val request = Request.Builder().url(wsUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                Log.d("WebSocketManager", "Conectado a $wsUrl")
                listener?.onOpen(webSocket, response)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                lastReceivedMessage = text
                Log.d("WebSocketManager", "Mensaje recibido: $text")
                listener?.onMessage(webSocket, text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocketManager", "Cerrando: $code / $reason")
                listener?.onClosing(webSocket, code, reason)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                Log.e("WebSocketManager", "Error: ${t.message}")
                listener?.onFailure(webSocket, t, response)
            }
        })
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

    fun isConnected(): Boolean {
        return webSocket != null && try {
            webSocket?.send("") == true
        } catch (e: Exception) {
            false
        }
    }
}