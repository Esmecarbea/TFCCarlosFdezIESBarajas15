package com.tfc.fat14

import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import com.airbnb.lottie.LottieAnimationView
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import com.google.android.material.button.MaterialButton

class AccesoActivity : AppCompatActivity() {

    // --- Variables Miembro ---
    private lateinit var lottieEsperandoCamara: LottieAnimationView
    private lateinit var textoEsperandoCamara: TextView
    private lateinit var webViewStream: WebView
    // private lateinit var mJavaDetector: CascadeClassifier // Comentado si no se usa OpenCV
    // private var absoluteFaceSize = 0                  // Comentado si no se usa OpenCV
    // private lateinit var webSocketManager: WebSocketManager // Comentado si WS se maneja en MainActivity


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acceso) // Establece el layout XML

        // --- Referencias a Vistas ---
        lottieEsperandoCamara = findViewById(R.id.lottie_esperando_camara)
        textoEsperandoCamara = findViewById(R.id.texto_esperando_camara)
        webViewStream = findViewById(R.id.web_view_stream)
        // Obtener referencias a los botones nuevos AHORA
        val botonRegistrarCara = findViewById<MaterialButton>(R.id.boton_registrar_cara)
        val botonGestionarCaras = findViewById<MaterialButton>(R.id.boton_gestionar_caras)


        // --- Configuración y Carga del WebView ---
        try {
            webViewStream.settings.javaScriptEnabled = true
            webViewStream.settings.loadWithOverviewMode = true
            webViewStream.settings.useWideViewPort = true
            webViewStream.settings.builtInZoomControls = false
            webViewStream.settings.displayZoomControls = false
            webViewStream.webViewClient = WebViewClient()

            val esp32Ip = "192.168.1.188" // <-- REVISA TU IP
            val streamUrl = "http://${esp32Ip}:81/stream"

            Log.d("AccesoActivity", "Cargando stream desde: $streamUrl")
            webViewStream.loadUrl(streamUrl)

        } catch (e: Exception) {
            Log.e("AccesoActivity", "Error configurando o cargando WebView: ${e.message}")
            textoEsperandoCamara.text = "Error al cargar vídeo" // Mostrar error
            textoEsperandoCamara.visibility = View.VISIBLE
            lottieEsperandoCamara.visibility = View.GONE
            webViewStream.visibility = View.GONE
            // Ocultar también los botones si falla la carga del vídeo
            botonRegistrarCara.visibility = View.GONE
            botonGestionarCaras.visibility = View.GONE
        }


        // --- Visibilidad Inicial y Retardo ---
        // Asegurarse de que los botones también empiezan ocultos si no hubo error
        if (webViewStream.visibility != View.GONE) { // Si no falló la carga
            webViewStream.visibility = View.GONE
            botonRegistrarCara.visibility = View.GONE
            botonGestionarCaras.visibility = View.GONE
        }
        lottieEsperandoCamara.visibility = View.VISIBLE
        textoEsperandoCamara.visibility = View.VISIBLE
        textoEsperandoCamara.text = getString(R.string.esperando_camara)


        // Retardo para mostrar WebView y botones
        Handler(Looper.getMainLooper()).postDelayed({
            Log.d("AccesoActivity", "Mostrando WebView y botones...")
            // Solo mostrar si no hubo error
            if (textoEsperandoCamara.text.toString() == getString(R.string.esperando_camara)) {
                lottieEsperandoCamara.visibility = View.GONE
                textoEsperandoCamara.visibility = View.GONE
                webViewStream.visibility = View.VISIBLE
                botonRegistrarCara.visibility = View.VISIBLE
                botonGestionarCaras.visibility = View.VISIBLE
            }
        }, 4000) // 4 segundos


        // --- WebSocket (Comentado por ahora) ---
        /*
        val myWebSocketListener = MyWebSocketListener()
        val wsUrl = "ws://${esp32Ip}:82/ws" // Puerto 82 para WS
        webSocketManager = WebSocketManager(myWebSocketListener, wsUrl )
        webSocketManager.connect()
        */

        // --- Clasificador OpenCV (Comentado) ---
        // loadClassifier()

    } // <-- Fin de onCreate


    // --- OTRAS FUNCIONES (Comentadas si no se usan) ---
    /*
    private fun loadClassifier() { ... }
    private fun detectFaces(bitmap: Bitmap): Bitmap { ... }
    inner class MyWebSocketListener : WebSocketListener() { ... }
    override fun onDestroy() { ... }
    override fun onPause() { ... }
     override fun onResume() { ... }
     */

}