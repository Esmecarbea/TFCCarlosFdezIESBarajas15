package com.tfc.fat14

import android.os.Bundle
import android.view.WindowManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.button.MaterialButton
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import android.widget.TextView
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.widget.SwitchCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class AccesoActivity : AppCompatActivity() {

    private lateinit var lottieEsperandoCamara: LottieAnimationView
    private lateinit var textoEsperandoCamara: TextView
    private lateinit var webViewStream: WebView
    private val myWebSocketListener: WebSocketListener by lazy { MyWebSocketListener() }
    private var isWaitingForEnrollment = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acceso)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        lottieEsperandoCamara = findViewById(R.id.lottie_esperando_camara)
        textoEsperandoCamara = findViewById(R.id.texto_esperando_camara)
        webViewStream = findViewById(R.id.web_view_stream)
        val switchRegistrarCara = findViewById<SwitchCompat>(R.id.switch_registrar_cara) // Cambiado a Switch
        val botonGestionarCaras = findViewById<MaterialButton>(R.id.boton_gestionar_caras)
        val esp32Ip = "192.168.105.44"
        val streamUrl = "http://${esp32Ip}:81/stream"

        // Inicializar y conectar el WebSocket
        MainActivity.webSocketManager?.let {
            it.connect(myWebSocketListener) // Conectar al WebSocket del ESP32 pasando el listener directamente
        } ?: run {
            Log.e("AccesoActivity", "WebSocketManager no está inicializado en MainActivity")
            textoEsperandoCamara.text = getString(R.string.error_websocket_no_inicializado)
            textoEsperandoCamara.visibility = View.VISIBLE
            lottieEsperandoCamara.visibility = View.GONE
            webViewStream.visibility = View.GONE
            switchRegistrarCara.visibility = View.GONE
            botonGestionarCaras.visibility = View.GONE
        }

        // Configuración del WebView
        webViewStream.settings.javaScriptEnabled = true
        webViewStream.settings.loadWithOverviewMode = true
        webViewStream.settings.useWideViewPort = true
        webViewStream.settings.builtInZoomControls = false
        webViewStream.settings.displayZoomControls = false
        webViewStream.settings.setSupportZoom(false)
        webViewStream.webViewClient = object : WebViewClient() {
            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                Log.e("AccesoActivity", "Error en WebView ($errorCode): $description en $failingUrl")
                if (failingUrl == streamUrl) {
                    runOnUiThread {
                        textoEsperandoCamara.text = getString(R.string.error_cargar_video, errorCode.toString())
                        textoEsperandoCamara.visibility = View.VISIBLE
                        lottieEsperandoCamara.visibility = View.GONE
                        webViewStream.visibility = View.GONE
                        switchRegistrarCara.visibility = View.GONE
                        botonGestionarCaras.visibility = View.GONE
                    }
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Log.d("AccesoActivity", "Página cargada: $url")
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                Log.d("AccesoActivity", "Iniciando carga de página: $url")
            }
        }

        // Cargar el WebView de forma asíncrona con un timeout
        CoroutineScope(Dispatchers.Main).launch {
            val loadSuccess = withTimeoutOrNull(10000) { // Timeout de 10 segundos
                try {
                    webViewStream.loadUrl(streamUrl)
                    true
                } catch (e: Exception) {
                    Log.e("AccesoActivity", "Error EXCEPCIÓN configurando o cargando WebView: ${e.message}")
                    false
                }
            }

            if (loadSuccess != true) {
                textoEsperandoCamara.text = getString(R.string.error_cargar_video_timeout)
                textoEsperandoCamara.visibility = View.VISIBLE
                lottieEsperandoCamara.visibility = View.GONE
                webViewStream.visibility = View.GONE
                switchRegistrarCara.visibility = View.GONE
                botonGestionarCaras.visibility = View.GONE
            }
        }

        // Listener del Switch
        switchRegistrarCara.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Log.d("AccesoActivity", "Switch Modo Registro: ON")
                MainActivity.webSocketManager?.sendMessage("activar_registro") // Enviar mensaje para activar
                textoEsperandoCamara.text = getString(R.string.modo_registro_activado)
                textoEsperandoCamara.visibility = View.VISIBLE
                isWaitingForEnrollment = true
            } else {
                Log.d("AccesoActivity", "Switch Modo Registro: OFF")
                MainActivity.webSocketManager?.sendMessage("desactivar_registro") // Enviar mensaje para desactivar
                textoEsperandoCamara.text = getString(R.string.modo_registro_desactivado)
                textoEsperandoCamara.visibility = View.VISIBLE
                isWaitingForEnrollment = false
            }
        }

        botonGestionarCaras.setOnClickListener {
            Log.d("AccesoActivity", "Botón Gestionar Caras pulsado")
            MainActivity.webSocketManager?.sendMessage("GESTIONAR_USUARIOS")
        }

        // Visibilidad inicial
        if (webViewStream.visibility != View.GONE) {
            webViewStream.visibility = View.GONE
            switchRegistrarCara.visibility = View.GONE
            botonGestionarCaras.visibility = View.GONE
        }
        lottieEsperandoCamara.visibility = View.VISIBLE
        textoEsperandoCamara.visibility = View.VISIBLE
        textoEsperandoCamara.text = getString(R.string.esperando_camara)

        Handler(Looper.getMainLooper()).postDelayed({
            if (textoEsperandoCamara.text.toString() != getString(R.string.error_cargar_video) && webViewStream.isAttachedToWindow) {
                Log.d("AccesoActivity", "Mostrando WebView y botones...")
                lottieEsperandoCamara.visibility = View.GONE
                textoEsperandoCamara.visibility = View.GONE
                webViewStream.visibility = View.VISIBLE
                switchRegistrarCara.visibility = View.VISIBLE
                botonGestionarCaras.visibility = View.VISIBLE
            } else {
                Log.d("AccesoActivity", "No se muestra WebView debido a error o actividad destruida")
            }
        }, 4000)
    }

    override fun onDestroy() {
        super.onDestroy()
        MainActivity.webSocketManager?.close()
    }

    inner class MyWebSocketListener : WebSocketListener() {
        private var lastUpdateTime = 0L
        private val updateInterval = 500L

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d("AccesoActivity", "WebSocket abierto")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val currentTime = System.currentTimeMillis()
            Log.d("AccesoActivity", "Mensaje WS recibido (crudo): $text") // Log crudo para depurar
            if (currentTime - lastUpdateTime >= updateInterval) {
                runOnUiThread {
                    when (text) {
                        "Intruder" -> {
                            textoEsperandoCamara.text = getString(R.string.intruso_detectado)
                            Log.d("AccesoActivity", "Intruso detectado mostrado en UI")
                        }
                        "Enrolling face..." -> {
                            textoEsperandoCamara.text = getString(R.string.registrando_cara)
                            Log.d("AccesoActivity", "Registrando cara mostrado en UI")
                        }
                        "Enrollment failed or maximum faces reached" -> {
                            textoEsperandoCamara.text = getString(R.string.limite_alcanzado_fallo_registrar)
                            isWaitingForEnrollment = false
                            Log.d("AccesoActivity", "Fallo al registrar mostrado en UI")
                        }
                        "registrar_usuario_activado" -> {
                            textoEsperandoCamara.text = getString(R.string.modo_registro_activado)
                            Log.d("AccesoActivity", "Modo de registro activado mostrado en UI")
                        }
                        "registrar_usuario_desactivado" -> {
                            textoEsperandoCamara.text = getString(R.string.modo_registro_desactivado)
                            Log.d("AccesoActivity", "Modo de registro desactivado mostrado en UI")
                        }
                        else -> {
                            if (text.startsWith("Face enrolled with ID: ")) {
                                val id = text.removePrefix("Face enrolled with ID: ").trim()
                                textoEsperandoCamara.text = getString(R.string.usuario_registrado_con_id, id)
                                isWaitingForEnrollment = false
                                Log.d("AccesoActivity", "Usuario registrado con ID: $id mostrado en UI")
                            } else {
                                Log.w("AccesoActivity", "Mensaje WS no manejado: $text")
                            }
                        }
                    }
                }
                lastUpdateTime = currentTime
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d("AccesoActivity", "WebSocket cerrando: $code / $reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            runOnUiThread {
                textoEsperandoCamara.text = getString(R.string.error_websocket, t.message)
                Log.e("AccesoActivity", "WebSocket Error: ${t.message}", t)
                isWaitingForEnrollment = false
            }
        }
    }
}