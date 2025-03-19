package com.tfc.fat13

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView

class AccesoActivity : AppCompatActivity() {

    private lateinit var lottieEsperandoCamara: LottieAnimationView
    private lateinit var textoEsperandoCamara: TextView
    private lateinit var videoStreamView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acceso)

        // Inicialización de las variables para los componentes de la UI
        lottieEsperandoCamara = findViewById(R.id.lottie_esperando_camara)
        textoEsperandoCamara = findViewById(R.id.texto_esperando_camara)
        videoStreamView = findViewById(R.id.video_stream_view)

        Handler(Looper.getMainLooper()).postDelayed({
            // Código a ejecutar después de 4 segundos
            lottieEsperandoCamara.visibility = View.GONE
            textoEsperandoCamara.visibility = View.GONE
            videoStreamView.visibility = View.VISIBLE

            Thread.sleep(500)

            val videoUri = Uri.parse("http://192.168.1.163:81/stream")

            videoStreamView.setVideoURI(videoUri)

            videoStreamView.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.start() // Inicia la reproducción
            }
            // Listener para errores (OPCIONAL, pero recomendado)
            videoStreamView.setOnErrorListener { mediaPlayer, what, extra ->
                println("Error en VideoView: What: $what, Extra: $extra")
                true // Indica que hemos manejado el error
            }
        }, 4000) // 4000 milisegundos = 4 segundos
    }
}