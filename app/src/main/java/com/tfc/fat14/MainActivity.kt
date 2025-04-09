package com.tfc.fat14

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class MainActivity : AppCompatActivity() {

    companion object {
        var webSocketManager: WebSocketManager? = null
    }

    private lateinit var botonIrAcceso: MaterialButton
    private lateinit var botonModoTaller: MaterialButton
    private lateinit var myWebSocketListener: MyWebSocketListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        botonIrAcceso = findViewById(R.id.boton_ir_acceso)
        botonModoTaller = findViewById(R.id.boton_modo_taller)
        myWebSocketListener = MyWebSocketListener()

        // Inicializar y conectar WebSocket solo si no está conectado
        if (webSocketManager == null) {
            webSocketManager = WebSocketManager()
            webSocketManager?.connect(myWebSocketListener)
        }

        botonIrAcceso.setOnClickListener {
            // Forzar conexión WebSocket si no está conectado
            if (webSocketManager == null || webSocketManager?.isConnected() != true) {
                webSocketManager = WebSocketManager()
                webSocketManager?.connect(myWebSocketListener)
                Log.d("MainActivity", "Reconectando WebSocket para Acceso")
            }
            val intent = Intent(this, AccesoActivity::class.java)
            startActivity(intent)
        }

        botonModoTaller.setOnClickListener {
            // Forzar conexión WebSocket si no está conectado
            if (webSocketManager == null || webSocketManager?.isConnected() != true) {
                webSocketManager = WebSocketManager()
                webSocketManager?.connect(myWebSocketListener)
                Log.d("MainActivity", "Reconectando WebSocket para Modo Taller")
            }
            val intent = Intent(this, ModoTallerActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // No cerramos el WebSocket aquí para que persista entre actividades
        // webSocketManager?.close()
        Log.d("MainActivity", "onDestroy ejecutado")
    }

    inner class MyWebSocketListener : WebSocketListener() {
        private var lastUpdateTime = 0L
        private val updateInterval = 500L // Actualizar la UI cada 500ms

        override fun onOpen(webSocket: WebSocket, response: Response) {
            runOnUiThread {
                Log.d("MainActivity", "WebSocket Conectado")
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdateTime >= updateInterval) {
                runOnUiThread {
                    Log.d("MainActivity", "Mensaje WS recibido: $text")
                    when (text) {
                        "ESTADO:TALLER_ACTIVO" -> Log.d("MainActivity", "Modo Taller: ACTIVADO")
                        "ESTADO:NORMAL_RELE_ON" -> Log.d("MainActivity", "Modo Normal: Relé ON")
                        "ESTADO:NORMAL_RELE_OFF" -> Log.d("MainActivity", "Modo Normal: Relé OFF")
                        else -> Log.w("MainActivity", "Mensaje WS no manejado aquí: $text")
                    }
                }
                lastUpdateTime = currentTime
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            runOnUiThread {
                Log.d("MainActivity", "WebSocket cerrando: $code / $reason")
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            runOnUiThread {
                Log.e("MainActivity", "WebSocket Error: ${t.message}", t)
            }
        }
    }
}