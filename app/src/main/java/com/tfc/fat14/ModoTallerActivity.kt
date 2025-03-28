package com.tfc.fat14

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class ModoTallerActivity : AppCompatActivity() {
    private lateinit var webSocketManager: WebSocketManager
    private lateinit var colorAnim: ObjectAnimator
    private lateinit var textoModoTaller: TextView
    private var anim: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modo_taller)

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

        // Referencias a los elementos de la UI
        textoModoTaller = findViewById(R.id.texto_modo_taller)
        val botonConfirmar = findViewById<MaterialButton>(R.id.boton_confirmar)
        val botonCancelar = findViewById<MaterialButton>(R.id.boton_cancelar)
        val botonDesactivar = findViewById<MaterialButton>(R.id.boton_desactivar)

        // Listener del botón Cancelar
        botonCancelar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Listener del botón Confirmar
        botonConfirmar.setOnClickListener {
            webSocketManager.sendMessage("MODO_TALLER_ACTIVADO")
            Log.d("WebSocket", "Enviado: MODO_TALLER_ACTIVADO")

            colorAnim.end()
            anim?.end()

            textoModoTaller.text = "MODO TALLER ACTIVADO"
            textoModoTaller.setTextColor(ContextCompat.getColor(this, R.color.new_red))

            anim = ObjectAnimator.ofFloat(textoModoTaller, View.ALPHA, 0.0f, 1.0f).apply {
                duration = 500
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = ObjectAnimator.INFINITE
                start()
            }

            botonCancelar.visibility = View.GONE
            botonConfirmar.visibility = View.GONE
            botonDesactivar.visibility = View.VISIBLE
        }

        // Listener del botón Desactivar
        botonDesactivar.setOnClickListener {
            webSocketManager.sendMessage("MODO_TALLER_DESACTIVADO")
            Log.d("WebSocket", "Enviado: MODO_TALLER_DESACTIVADO")

            textoModoTaller.text = getString(R.string.texto_modo_taller)
            textoModoTaller.setTextColor(ContextCompat.getColor(this, R.color.white))

            anim?.end()

            botonCancelar.visibility = View.VISIBLE
            botonConfirmar.visibility = View.VISIBLE
            botonDesactivar.visibility = View.GONE
        }

        // Animación inicial del texto
        colorAnim = ObjectAnimator.ofInt(
            textoModoTaller,
            "textColor",
            ContextCompat.getColor(this, R.color.white),
            ContextCompat.getColor(this, android.R.color.transparent)
        ).apply {
            duration = 1000
            setEvaluator(ArgbEvaluator())
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            start()
        }
    }
//prueba:qw
    override fun onDestroy() {
        super.onDestroy()
        webSocketManager.close()
        colorAnim.end()
        anim?.end()
    }
}