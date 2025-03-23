package com.tfc.fat13

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.glance.visibility
import com.google.android.material.button.MaterialButton

class ModoTallerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modo_taller)

        // Referencia al TextView
        val textoModoTaller = findViewById<TextView>(R.id.texto_modo_taller)
        // Referencia al boton confirmar
        val botonConfirmar = findViewById<MaterialButton>(R.id.boton_confirmar)
        // Referencia al boton cancelar
        val botonCancelar = findViewById<MaterialButton>(R.id.boton_cancelar)
        // Referencia al boton desactivar
        val botonDesactivar = findViewById<MaterialButton>(R.id.boton_desactivar)



        // Listener del boton cancelar
        botonCancelar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Listener del boton confirmar
        botonConfirmar.setOnClickListener {
            /// Cambiar el texto
            textoModoTaller.text = "MODO TALLER ACTIVADO"

            // Cambiar el color del texto
            textoModoTaller.setTextColor(ContextCompat.getColor(this, R.color.new_red))

            // Hacer parpadear el texto
            val anim = ObjectAnimator.ofFloat(textoModoTaller, "alpha", 0.0f, 1.0f)
            anim.duration = 500 // Duración de la animación en milisegundos
            anim.repeatMode = ObjectAnimator.REVERSE
            anim.repeatCount = ObjectAnimator.INFINITE
            anim.start()

            // Ocultar el botón "CANCELAR"
            botonCancelar.visibility = android.view.View.GONE

            // Ocultar el botón "CONFIRMAR"
            botonConfirmar.visibility = android.view.View.GONE

            // Mostrar el botón "DESACTIVAR"
            botonDesactivar.visibility = android.view.View.VISIBLE

            //Enviar señal a ESP32 (esta parte se implementara en otro paso)
            enviarSenalESP32()
        }
        //Listener del boton desactivar
        botonDesactivar.setOnClickListener {
            // Restaurar el texto original
            textoModoTaller.text = getString(R.string.texto_modo_taller)

            // Restaurar el color del texto a blanco
            textoModoTaller.setTextColor(ContextCompat.getColor(this, R.color.white))

            // Detener el parpadeo
            textoModoTaller.clearAnimation()

            // Mostrar el botón "CANCELAR"
            botonCancelar.visibility = android.view.View.VISIBLE

            // Mostrar el botón "CONFIRMAR"
            botonConfirmar.visibility = android.view.View.VISIBLE

            // Ocultar el botón "DESACTIVAR"
            botonDesactivar.visibility = android.view.View.GONE


        }

        //Enviar señal a ESP32
        enviarSenalESP32()

        val colorAnim = ObjectAnimator.ofInt(textoModoTaller, "textColor", 0xFFFFFFFF.toInt(), 0x00FFFFFF)
        colorAnim.setDuration(1000)
        colorAnim.setEvaluator(ArgbEvaluator())
        colorAnim.setRepeatCount(ObjectAnimator.INFINITE)
        colorAnim.setRepeatMode(ObjectAnimator.REVERSE)
        colorAnim.start()
    }
    private fun enviarSenalESP32() {
        // En este momento aun no tenemos la funcionalidad para la esp32
        println("Enviando señal a la ESP32")
    }
}