package com.tfc.fat13

import android.animation.ArgbEvaluator
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
        val colorAnim = ObjectAnimator.ofInt(textoModoTaller, "textColor", 0xFFFFFFFF.toInt(), 0x00FFFFFF)
        colorAnim.setDuration(1000)
        colorAnim.setEvaluator(ArgbEvaluator())
        colorAnim.setRepeatCount(ObjectAnimator.INFINITE)
        colorAnim.setRepeatMode(ObjectAnimator.REVERSE)
        colorAnim.start()
    }
}