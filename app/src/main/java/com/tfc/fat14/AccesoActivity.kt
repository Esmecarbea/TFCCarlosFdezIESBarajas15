package com.tfc.fat14

import android.os.Bundle
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

        lottieEsperandoCamara = findViewById(R.id.lottie_esperando_camara)
        textoEsperandoCamara = findViewById(R.id.texto_esperando_camara)
        webViewStream = findViewById(R.id.web_view_stream)
        val botonRegistrarCara = findViewById<MaterialButton>(R.id.boton_registrar_cara)
        val botonGestionarCaras = findViewById<MaterialButton>(R.id.boton_gestionar_caras)
        val esp32Ip = "192.168.1.188"
        val streamUrl = "http://${esp32Ip}:81/stream"

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
                        textoEsperandoCamara.text = "Error al cargar vídeo ($errorCode)"
                        textoEsperandoCamara.visibility = View.VISIBLE
                        lottieEsperandoCamara.visibility = View.GONE
                        webViewStream.visibility = View.GONE
                        botonRegistrarCara.visibility = View.GONE
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
                textoEsperandoCamara.text = "Error al cargar vídeo: Timeout"
                textoEsperandoCamara.visibility = View.VISIBLE
                lottieEsperandoCamara.visibility = View.GONE
                webViewStream.visibility = View.GONE
                botonRegistrarCara.visibility = View.GONE
                botonGestionarCaras.visibility = View.GONE
            }
        }

        // Listeners de botones
        botonRegistrarCara.setOnClickListener {
            Log.d("AccesoActivity", "Botón Registrar Cara pulsado")
            isWaitingForEnrollment = true
            MainActivity.webSocketManager?.sendMessage("enroll")
            textoEsperandoCamara.text = "Registrando cara..."
            textoEsperandoCamara.visibility = View.VISIBLE

            // Timeout para esperar la respuesta de la ESP32
            CoroutineScope(Dispatchers.Main).launch {
                withTimeoutOrNull(15000) { // Esperamos 15 segundos
                    while (isWaitingForEnrollment) {
                        kotlinx.coroutines.delay(100)
                    }
                }
                if (isWaitingForEnrollment) {
                    textoEsperandoCamara.text = "Error: No se recibió respuesta de la ESP32"
                    isWaitingForEnrollment = false
                }
            }
        }

        botonGestionarCaras.setOnClickListener {
            Log.d("AccesoActivity", "Botón Gestionar Caras pulsado")
            MainActivity.webSocketManager?.sendMessage("GESTIONAR_USUARIOS")
        }

        // Visibilidad inicial
        if (webViewStream.visibility != View.GONE) {
            webViewStream.visibility = View.GONE
            botonRegistrarCara.visibility = View.GONE
            botonGestionarCaras.visibility = View.GONE
        }
        lottieEsperandoCamara.visibility = View.VISIBLE
        textoEsperandoCamara.visibility = View.VISIBLE
        textoEsperandoCamara.text = getString(R.string.esperando_camara)

        Handler(Looper.getMainLooper()).postDelayed({
            if (textoEsperandoCamara.text.toString() != "Error al cargar vídeo" && webViewStream.isAttachedToWindow) {
                Log.d("AccesoActivity", "Mostrando WebView y botones...")
                lottieEsperandoCamara.visibility = View.GONE
                textoEsperandoCamara.visibility = View.GONE
                webViewStream.visibility = View.VISIBLE
                botonRegistrarCara.visibility = View.VISIBLE
                botonGestionarCaras.visibility = View.VISIBLE
            } else {
                Log.d("AccesoActivity", "No se muestra WebView debido a error o actividad destruida")
            }
        }, 4000)
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
                            textoEsperandoCamara.text = "Intruso detectado"
                            Log.d("AccesoActivity", "Intruso detectado mostrado en UI")
                        }
                        "Enrolling face..." -> {
                            textoEsperandoCamara.text = "Registrando cara..."
                            Log.d("AccesoActivity", "Registrando cara mostrado en UI")
                        }
                        "Enrollment failed or maximum faces reached" -> {
                            textoEsperandoCamara.text = "Límite alcanzado o fallo al registrar"
                            isWaitingForEnrollment = false
                            Log.d("AccesoActivity", "Fallo al registrar mostrado en UI")
                        }
                        else -> {
                            if (text.startsWith("Face enrolled with ID: ")) {
                                val id = text.removePrefix("Face enrolled with ID: ").trim()
                                textoEsperandoCamara.text = "Usuario registrado con ID: $id"
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
                textoEsperandoCamara.text = "Error en WebSocket: ${t.message}"
                Log.e("AccesoActivity", "WebSocket Error: ${t.message}", t)
                isWaitingForEnrollment = false
            }
        }
    }
}