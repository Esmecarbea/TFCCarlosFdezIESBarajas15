package com.tfc.fat14

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class MainActivity : AppCompatActivity() {

    companion object {
        var webSocketManager: WebSocketManager? = null
    }

    private lateinit var botonIrAcceso: MaterialButton
    private lateinit var botonModoTaller: MaterialButton
    private lateinit var textoEstadoSistema: TextView
    private lateinit var textoConexionWs: TextView
    private lateinit var myWebSocketListener: MyWebSocketListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        botonIrAcceso = findViewById(R.id.boton_ir_acceso)
        botonModoTaller = findViewById(R.id.boton_modo_taller)
        textoEstadoSistema = findViewById(R.id.texto_estado_sistema)
        textoConexionWs = findViewById(R.id.texto_conexion_ws)
        myWebSocketListener = MyWebSocketListener()

        // Inicializar y conectar WebSocket solo si no está conectado
        if (webSocketManager == null) {
            webSocketManager = WebSocketManager()
            webSocketManager?.connect(myWebSocketListener)
        }

        textoEstadoSistema.text = "Estado: Desconocido"
        textoConexionWs.text = "WS: Conectando..."

        botonIrAcceso.setOnClickListener {
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
        // No cerramos el WebSocket aquí para que persista entre actividades
        // webSocketManager?.close()
        Log.d("MainActivity", "onDestroy ejecutado")
    }

    inner class MyWebSocketListener : WebSocketListener() {
        private var lastUpdateTime = 0L
        private val updateInterval = 500L // Actualizar la UI cada 500ms

        override fun onOpen(webSocket: WebSocket, response: Response) {
            runOnUiThread {
                textoConexionWs.text = "WS: Conectado"
                Log.d("MainActivity", "WebSocket Conectado")
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdateTime >= updateInterval) {
                runOnUiThread {
                    Log.d("MainActivity", "Mensaje WS recibido: $text")
                    when (text) {
                        "ESTADO:TALLER_ACTIVO" -> textoEstadoSistema.text = "Modo Taller: ACTIVADO"
                        "ESTADO:NORMAL_RELE_ON" -> textoEstadoSistema.text = "Modo Normal: Relé ON"
                        "ESTADO:NORMAL_RELE_OFF" -> textoEstadoSistema.text = "Modo Normal: Relé OFF"
                        else -> Log.w("MainActivity", "Mensaje WS no manejado aquí: $text")
                    }
                }
                lastUpdateTime = currentTime
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            runOnUiThread {
                textoConexionWs.text = "WS: Cerrando..."
                Log.d("MainActivity", "WebSocket cerrando: $code / $reason")
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            runOnUiThread {
                textoConexionWs.text = "WS: Error"
                Log.e("MainActivity", "WebSocket Error: ${t.message}", t)
            }
        }
    }
}