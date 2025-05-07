package com.tfc.fat14

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class ModoTallerActivity : AppCompatActivity() {
    private lateinit var colorAnim: ObjectAnimator
    private lateinit var textoModoTaller: TextView
    private lateinit var switchModoTaller: SwitchCompat
    private lateinit var circleTextView: TextView
    private lateinit var botonSalir: MaterialButton
    private lateinit var sharedPreferences: SharedPreferences
    private var anim: ObjectAnimator? = null

    companion object {
        private const val PREFS_NAME = "ModoTallerPrefs"
        private const val KEY_SWITCH_STATE = "switch_state"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modo_taller)

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Referencias a los elementos de la UI
        textoModoTaller = findViewById(R.id.texto_modo_taller)
        switchModoTaller = findViewById(R.id.switch_modo_taller)
        circleTextView = findViewById(R.id.circle_text_view)
        botonSalir = findViewById(R.id.boton_salir)

        // Restaurar el estado del Switch desde SharedPreferences
        val isSwitchOn = sharedPreferences.getBoolean(KEY_SWITCH_STATE, false)
        switchModoTaller.isChecked = isSwitchOn

        // Actualizar la UI según el estado inicial del Switch
        if (isSwitchOn) {
            textoModoTaller.setText(R.string.modo_taller_activado)
            textoModoTaller.setTextColor(ContextCompat.getColor(this, R.color.new_red))
            circleTextView.setText(R.string.switch_on)
            circleTextView.setBackgroundResource(R.drawable.circle_red)

            // Iniciar animación de parpadeo alfa
            anim = ObjectAnimator.ofFloat(textoModoTaller, View.ALPHA, 0.0f, 1.0f).apply {
                duration = 500
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = ObjectAnimator.INFINITE
                start()
            }
        } else {
            textoModoTaller.text = getString(R.string.texto_modo_taller)
            textoModoTaller.setTextColor(ContextCompat.getColor(this, R.color.white))
            textoModoTaller.alpha = 1.0f
            circleTextView.setText(R.string.switch_off)
            circleTextView.setBackgroundResource(R.drawable.circle_green)

            // Iniciar animación de color inicial
            startInitialColorAnimation()
        }

        // Listener del Switch
        switchModoTaller.setOnCheckedChangeListener { _, isChecked ->
            // Guardar el estado del Switch en SharedPreferences
            sharedPreferences.edit().putBoolean(KEY_SWITCH_STATE, isChecked).apply()

            if (isChecked) {
                // Activar modo taller
                Log.d("ModoTallerActivity", "Switch cambiado a ON - Activando modo taller")
                val wsManager = MainActivity.webSocketManager
                if (wsManager != null) {
                    wsManager.sendMessage("MODO_TALLER_ACTIVADO")
                    Log.d("WebSocket", "Enviado: MODO_TALLER_ACTIVADO")
                } else {
                    Log.e("WebSocket", "Error: WebSocketManager es null")
                }

                // Actualizar UI
                if (::colorAnim.isInitialized && colorAnim.isRunning) {
                    colorAnim.end()
                }
                anim?.cancel()

                textoModoTaller.setText(R.string.modo_taller_activado)
                textoModoTaller.setTextColor(ContextCompat.getColor(this, R.color.new_red))

                // Iniciar animación de parpadeo alfa
                anim = ObjectAnimator.ofFloat(textoModoTaller, View.ALPHA, 0.0f, 1.0f).apply {
                    duration = 500
                    repeatMode = ObjectAnimator.REVERSE
                    repeatCount = ObjectAnimator.INFINITE
                    start()
                }

                // Actualizar texto y fondo del círculo
                circleTextView.setText(R.string.switch_on)
                circleTextView.setBackgroundResource(R.drawable.circle_red)
            } else {
                // Desactivar modo taller
                Log.d("ModoTallerActivity", "Switch cambiado a OFF - Desactivando modo taller")
                val wsManager = MainActivity.webSocketManager
                if (wsManager != null) {
                    wsManager.sendMessage("MODO_TALLER_DESACTIVADO")
                    Log.d("WebSocket", "Enviado: MODO_TALLER_DESACTIVADO")
                } else {
                    Log.e("WebSocket", "Error: WebSocketManager es null")
                }

                // Restaurar texto y color
                textoModoTaller.text = getString(R.string.texto_modo_taller)
                textoModoTaller.setTextColor(ContextCompat.getColor(this, R.color.white))
                textoModoTaller.alpha = 1.0f

                // Detener animación de parpadeo
                anim?.cancel()

                // Iniciar animación de color inicial
                startInitialColorAnimation()

                // Actualizar texto y fondo del círculo
                circleTextView.setText(R.string.switch_off)
                circleTextView.setBackgroundResource(R.drawable.circle_green)
            }
        }

        // Listener del botón Salir
        botonSalir.setOnClickListener {
            Log.d("ModoTallerActivity", "Botón SALIR pulsado - Volviendo a MainActivity.")
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // Si el Switch está en OFF, la animación inicial ya se inició en el bloque anterior
    }

    // Función separada para iniciar la animación de color inicial
    private fun startInitialColorAnimation() {
        if (::colorAnim.isInitialized && colorAnim.isRunning) {
            colorAnim.cancel()
        }
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
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) { textoModoTaller.alpha = 1.0f }
                override fun onAnimationCancel(animation: Animator) { textoModoTaller.alpha = 1.0f }
                override fun onAnimationRepeat(animation: Animator) {}
            })
            start()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::colorAnim.isInitialized && colorAnim.isRunning) {
            colorAnim.pause()
        }
        anim?.pause()
        Log.d("ModoTallerActivity", "onPause llamado")
    }

    override fun onResume() {
        super.onResume()
        if (::colorAnim.isInitialized && colorAnim.isPaused) {
            colorAnim.resume()
        }
        anim?.resume()
        Log.d("ModoTallerActivity", "onResume llamado")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::colorAnim.isInitialized) {
            colorAnim.cancel()
        }
        anim?.cancel()
        Log.d("ModoTallerActivity", "onDestroy llamado")
    }
}