package com.tfc.fat14

// import android.graphics.Bitmap // Comentado si no se usa OpenCV
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
// import android.widget.ImageView // Comentado si no se usa OpenCV
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
// import androidx.core.graphics.createBitmap // Comentado si no se usa OpenCV
import com.airbnb.lottie.LottieAnimationView
// import okhttp3.Response // Comentado si no se usa WS aquí
// import okhttp3.WebSocket // Comentado si no se usa WS aquí
// import okhttp3.WebSocketListener // Comentado si no se usa WS aquí
// import org.opencv.android.Utils // Comentado si no se usa OpenCV
// import org.opencv.core.Mat // Comentado si no se usa OpenCV
// import org.opencv.core.MatOfRect // Comentado si no se usa OpenCV
// import org.opencv.core.Scalar // Comentado si no se usa OpenCV
// import org.opencv.imgproc.Imgproc // Comentado si no se usa OpenCV
// import org.opencv.objdetect.CascadeClassifier // Comentado si no se usa OpenCV
// import java.io.File // Comentado si no se usa OpenCV
// import java.io.FileOutputStream // Comentado si no se usa OpenCV
// import java.io.IOException // Comentado si no se usa OpenCV
// import java.io.InputStream // Comentado si no se usa OpenCV
import com.google.android.material.button.MaterialButton

class AccesoActivity : AppCompatActivity() {

    // --- Variables Miembro ---
    private lateinit var lottieEsperandoCamara: LottieAnimationView
    private lateinit var textoEsperandoCamara: TextView
    private lateinit var webViewStream: WebView
    // private lateinit var mJavaDetector: CascadeClassifier // Comentado si no se usa OpenCV
    // private var absoluteFaceSize = 0                  // Comentado si no se usa OpenCV
    // --- NO necesitamos webSocketManager aquí, usaremos el de MainActivity ---


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acceso) // Establece el layout XML

        // --- Referencias a Vistas ---
        lottieEsperandoCamara = findViewById(R.id.lottie_esperando_camara)
        textoEsperandoCamara = findViewById(R.id.texto_esperando_camara)
        webViewStream = findViewById(R.id.web_view_stream)
        // Obtener referencias a los botones nuevos
        val botonRegistrarCara = findViewById<MaterialButton>(R.id.boton_registrar_cara)
        val botonGestionarCaras = findViewById<MaterialButton>(R.id.boton_gestionar_caras)
        val esp32Ip = "192.168.1.188" // <-- REVISA TU IP
        val streamUrl = "http://${esp32Ip}:81/stream" // <-- Puerto 81 y /stream para video

        // --- Configuración y Carga del WebView ---
        try {
            webViewStream.settings.javaScriptEnabled = true
            webViewStream.settings.loadWithOverviewMode = true
            webViewStream.settings.useWideViewPort = true
            webViewStream.settings.builtInZoomControls = false
            webViewStream.settings.displayZoomControls = false
            webViewStream.webViewClient = object : WebViewClient() {
                // Opcional: Manejar errores de carga del WebView si es necesario
                override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    Log.e("AccesoActivity", "Error en WebView ($errorCode): $description en $failingUrl")
                    // Mostrar error al usuario si falla la carga del stream
                    if (failingUrl == streamUrl) { // Comprobar si el error es del stream
                        textoEsperandoCamara.text = "Error al cargar vídeo ($errorCode)"
                        textoEsperandoCamara.visibility = View.VISIBLE
                        lottieEsperandoCamara.visibility = View.GONE
                        webViewStream.visibility = View.GONE
                        botonRegistrarCara.visibility = View.GONE
                        botonGestionarCaras.visibility = View.GONE
                    }
                }
            }


            Log.d("AccesoActivity", "Cargando stream desde: $streamUrl")
            webViewStream.loadUrl(streamUrl)

        } catch (e: Exception) {
            Log.e("AccesoActivity", "Error EXCEPCIÓN configurando o cargando WebView: ${e.message}")
            textoEsperandoCamara.text = "Error al cargar vídeo" // Mostrar error
            textoEsperandoCamara.visibility = View.VISIBLE
            lottieEsperandoCamara.visibility = View.GONE
            webViewStream.visibility = View.GONE
            // Ocultar también los botones si falla la carga del vídeo
            botonRegistrarCara.visibility = View.GONE
            botonGestionarCaras.visibility = View.GONE
        }


        // --- Añadir Listeners para los Botones ---
        botonRegistrarCara.setOnClickListener {
            Log.d("AccesoActivity", "Botón Registrar Cara pulsado")
            // Usamos el WebSocketManager de MainActivity para enviar el comando
            // Asegúrate que la ESP32 espera exactamente esta cadena "REGISTRAR_USUARIO"
            MainActivity.webSocketManager?.sendMessage("REGISTRAR_USUARIO")
            // Opcional: Mostrar feedback al usuario (ej. un Toast)
            // Toast.makeText(this, "Enviando comando Registrar...", Toast.LENGTH_SHORT).show()
        }

        botonGestionarCaras.setOnClickListener {
            Log.d("AccesoActivity", "Botón Gestionar Caras pulsado")
            // Usamos el WebSocketManager de MainActivity para enviar el comando
            // Asegúrate que la ESP32 espera exactamente esta cadena "GESTIONAR_USUARIOS"
            MainActivity.webSocketManager?.sendMessage("GESTIONAR_USUARIOS")
            // Opcional: Mostrar feedback al usuario (ej. un Toast)
            // Toast.makeText(this, "Enviando comando Gestionar...", Toast.LENGTH_SHORT).show()
        }
        // --- Fin Listeners Añadidos ---


        // --- Visibilidad Inicial y Retardo ---
        // Asegurarse de que los botones también empiezan ocultos si no hubo error de carga inicial
        if (webViewStream.visibility != View.GONE) {
            webViewStream.visibility = View.GONE // Ocultar WebView inicialmente
            botonRegistrarCara.visibility = View.GONE
            botonGestionarCaras.visibility = View.GONE
        }
        lottieEsperandoCamara.visibility = View.VISIBLE
        textoEsperandoCamara.visibility = View.VISIBLE
        textoEsperandoCamara.text = getString(R.string.esperando_camara)


        // Retardo para mostrar WebView y botones (solo si no hubo error)
        Handler(Looper.getMainLooper()).postDelayed({
            // Volver a comprobar si hubo error antes de mostrar
            if (textoEsperandoCamara.text.toString() != "Error al cargar vídeo") {
                Log.d("AccesoActivity", "Mostrando WebView y botones...")
                lottieEsperandoCamara.visibility = View.GONE
                textoEsperandoCamara.visibility = View.GONE
                webViewStream.visibility = View.VISIBLE
                botonRegistrarCara.visibility = View.VISIBLE
                botonGestionarCaras.visibility = View.VISIBLE
            } else {
                Log.d("AccesoActivity", "No se muestra WebView/botones debido a error previo.")
            }
        }, 4000) // 4 segundos


        // --- WebSocket (Sigue comentado aquí, se inicializa y usa desde MainActivity) ---
        /*
        val myWebSocketListener = MyWebSocketListener()
        val wsUrl = "ws://${esp32Ip}:82/ws" // Puerto 82 para WS
        webSocketManager = WebSocketManager(myWebSocketListener, wsUrl )
        webSocketManager.connect()
        */

        // --- Clasificador OpenCV (Comentado si no se usa aquí) ---
        // loadClassifier()

    } // <-- Fin de onCreate


    // --- OTRAS FUNCIONES (Comentadas si no se usan directamente aquí) ---
    /*
    private fun loadClassifier() { ... }
    private fun detectFaces(bitmap: Bitmap): Bitmap { ... }
    // El listener de WebSocket se maneja en MainActivity
    // inner class MyWebSocketListener : WebSocketListener() { ... }
    // Los ciclos de vida no necesitan manejar el WS aquí si se hace en Main
    // override fun onDestroy() { ... }
    // override fun onPause() { ... }
    // override fun onResume() { ... }
     */

} // --- Fin clase AccesoActivity ---