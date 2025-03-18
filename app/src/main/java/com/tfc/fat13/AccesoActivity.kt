package com.tfc.fat13

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

        // Handler para retrasar la desaparición del Lottie y la aparición del VideoView
        Handler(Looper.getMainLooper()).postDelayed({
            // Código a ejecutar después de 4 segundos
            lottieEsperandoCamara.visibility = View.GONE // Oculta la animación Lottie
            textoEsperandoCamara.visibility = View.GONE // Oculta el texto "Esperando Cámara"
            videoStreamView.visibility = View.VISIBLE // Muestra el VideoView
        }, 4000) // 4000 milisegundos = 4 segundos
    }
}