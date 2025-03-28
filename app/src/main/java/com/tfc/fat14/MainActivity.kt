package com.tfc.fat14

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.opencv.android.OpenCVLoader

class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var webSocketManager: WebSocketManager // Variable para usar el WebSocket en otras actividades
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!OpenCVLoader.initLocal()) {
            // Handle initialization error
            Log.e("OpenCV", "Error initializing OpenCV")
        } else {
            Log.d("OpenCV", "OpenCV initialized successfully")
        }

        // Crear un WebSocketListener
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Conexión abierta")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Mensaje recibido: $text")
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.d("WebSocket", "Mensaje binario recibido: $bytes")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Conexión cerrándose: $code, $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Conexión cerrada: $code, $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Error: ${t.message}")
            }
        }

        // Inicializar el WebSocketManager
        webSocketManager = WebSocketManager(listener)
        webSocketManager.connect()

        val botonAcceso: MaterialButton = findViewById(R.id.boton_acceso)
        val botonModoTaller: MaterialButton = findViewById(R.id.boton_modo_taller)

        botonAcceso.setOnClickListener {
            val intent = Intent(this, AccesoActivity::class.java)
            startActivity(intent)
        }

        botonModoTaller.setOnClickListener {
            val intent = Intent(this, ModoTallerActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketManager.close()
    }
}