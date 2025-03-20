package com.tfc.fat13

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ModoTallerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modo_taller)

        val botonCancelar: Button = findViewById(R.id.boton_cancelar)
        val botonConfirmar: Button = findViewById(R.id.boton_confirmar)
        val textoModoTaller: TextView = findViewById(R.id.texto_modo_taller)

        botonCancelar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        botonConfirmar.setOnClickListener {
            // Por ahora no hacemos nada al pulsar "Confirmar"
        }

        val animacionParpadeo = ObjectAnimator.ofFloat(textoModoTaller, "alpha", 1f, 0f)
        animacionParpadeo.duration = 500 // Duración de cada parpadeo en milisegundos
        animacionParpadeo.repeatMode = ObjectAnimator.REVERSE // Se invierte al final
        animacionParpadeo.repeatCount = ObjectAnimator.INFINITE // Se repite infinitamente
        animacionParpadeo.start()
    }
}