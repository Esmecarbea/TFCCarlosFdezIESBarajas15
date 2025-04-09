package com.tfc.fat14

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class ModoTallerActivity : AppCompatActivity() {
    private lateinit var colorAnim: ObjectAnimator
    private lateinit var textoModoTaller: TextView
    private var anim: ObjectAnimator? = null

    // Variable para almacenar la referencia al WebSocketManager global
    // Asumimos que está inicializado en MainActivity
    // private lateinit var webSocketManager: WebSocketManager // O usar directamente MainActivity.webSocketManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modo_taller)

        // Inicializar WebSocketManager (si no se usa el estático de MainActivity)
        // Asegúrate de que MainActivity.webSocketManager está inicializado antes de llamar a esta actividad
        // webSocketManager = MainActivity.webSocketManager // Ejemplo

        // Referencias a los elementos de la UI
        textoModoTaller = findViewById(R.id.texto_modo_taller)
        val botonConfirmar = findViewById<MaterialButton>(R.id.boton_confirmar)
        val botonCancelar = findViewById<MaterialButton>(R.id.boton_cancelar)
        val botonDesactivar = findViewById<MaterialButton>(R.id.boton_desactivar)
        val botonSalirTaller = findViewById<MaterialButton>(R.id.boton_salir_taller) // <-- NUEVA REFERENCIA

        // Listener del botón Cancelar
        botonCancelar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Cierra esta actividad al cancelar
        }

        // Listener del botón Confirmar
        botonConfirmar.setOnClickListener {
            Log.d("ModoTallerActivity", "Botón CONFIRMAR pulsado")
            val wsManager = MainActivity.webSocketManager
            if (wsManager != null) {
                wsManager.sendMessage("MODO_TALLER_ACTIVADO")
                Log.d("WebSocket", "Enviado: MODO_TALLER_ACTIVADO")
            } else {
                Log.e("WebSocket", "Error: WebSocketManager es null")
            }

            // --- Gestión UI ---
            // Detener animación inicial si está corriendo
            if (::colorAnim.isInitialized && colorAnim.isRunning) {
                colorAnim.end()
            }
            anim?.cancel() // Cancelar animación de parpadeo si existiera

            textoModoTaller.setText(R.string.modo_taller_activado)
            textoModoTaller.setTextColor(ContextCompat.getColor(this, R.color.new_red))

            // Iniciar animación de parpadeo alfa
            anim = ObjectAnimator.ofFloat(textoModoTaller, View.ALPHA, 0.0f, 1.0f).apply {
                duration = 500
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = ObjectAnimator.INFINITE
                start()
            }

            // Ajustar visibilidad de botones
            botonCancelar.visibility = View.GONE
            botonConfirmar.visibility = View.GONE
            botonDesactivar.visibility = View.VISIBLE
            botonSalirTaller.visibility = View.VISIBLE // <-- HACER VISIBLE SALIR
        }

        // Listener del botón Desactivar
        botonDesactivar.setOnClickListener {
            Log.d("ModoTallerActivity", "Botón DESACTIVAR pulsado")
            val wsManager = MainActivity.webSocketManager
            if (wsManager != null) {
                wsManager.sendMessage("MODO_TALLER_DESACTIVADO")
                Log.d("WebSocket", "Enviado: MODO_TALLER_DESACTIVADO")
            } else {
                Log.e("WebSocket", "Error: WebSocketManager es null")
            }

            // --- Gestión UI ---
            // Restaurar texto y color
            textoModoTaller.text = getString(R.string.texto_modo_taller)
            textoModoTaller.setTextColor(ContextCompat.getColor(this, R.color.white))
            textoModoTaller.alpha = 1.0f // Asegurar que no se quede transparente

            // Detener animación de parpadeo
            anim?.cancel() // Usar cancel() es a menudo más seguro que end()

            // Iniciar animación de color inicial de nuevo
            startInitialColorAnimation() // Llamar a función para reiniciar animación

            // Ajustar visibilidad de botones
            botonCancelar.visibility = View.VISIBLE
            botonConfirmar.visibility = View.VISIBLE
            botonDesactivar.visibility = View.GONE
            botonSalirTaller.visibility = View.GONE // <-- HACER INVISIBLE SALIR
        }

        // Listener del NUEVO botón Salir
        botonSalirTaller.setOnClickListener {
            Log.d("ModoTallerActivity", "Botón SALIR pulsado - Volviendo a MainActivity.")
            // Simplemente volvemos a la actividad principal, sin enviar mensaje WS
            val intent = Intent(this, MainActivity::class.java)
            // Flags para evitar crear múltiples instancias de MainActivity si ya existe
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish() // Cierra esta actividad
        }

        // Iniciar Animación inicial del texto
        startInitialColorAnimation()
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
            ContextCompat.getColor(this, android.R.color.transparent) // Parpadea a transparente
        ).apply {
            duration = 1000
            setEvaluator(ArgbEvaluator())
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            // Añadir listener para asegurar que el alpha es 1 al final si se cancela
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
        // Detener animaciones si la actividad se pausa para ahorrar recursos
        if (::colorAnim.isInitialized && colorAnim.isRunning) {
            colorAnim.pause() // Pausar en lugar de terminar
        }
        anim?.pause()
        // Considerar si realmente quieres cerrar el WS en onPause
        // MainActivity.webSocketManager.close() // Comentado por ahora
        Log.d("ModoTallerActivity", "onPause llamado")

    }

    override fun onResume() {
        super.onResume()
        // Reanudar animaciones si estaban pausadas
        if (::colorAnim.isInitialized && colorAnim.isPaused) {
            colorAnim.resume()
        }
        anim?.resume()
        // Considerar reconectar el WS si se cerró en onPause y es necesario
        // if (!MainActivity.webSocketManager.isConnected()) { MainActivity.webSocketManager.connect() }
        Log.d("ModoTallerActivity", "onResume llamado")
    }


    override fun onDestroy() {
        super.onDestroy()
        // Detener y limpiar animaciones definitivamente
        if (::colorAnim.isInitialized) {
            colorAnim.cancel()
        }
        anim?.cancel()

        // Cerrar el WebSocket si la actividad se destruye permanentemente
        // Esto depende de si MainActivity mantiene vivo el WebSocketManager o no.
        // Si el Manager es estático en MainActivity, quizás no quieras cerrarlo aquí.
        // MainActivity.webSocketManager.close() // Comentado: evaluar si es necesario
        Log.d("ModoTallerActivity", "onDestroy llamado")
    }
}