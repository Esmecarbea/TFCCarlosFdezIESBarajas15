package com.tfc.fat13

import com.google.android.material.button.MaterialButton
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val botonAcceso = findViewById<MaterialButton>(R.id.boton_acceso) // <-- Encuentra el botón por su ID
        botonAcceso.setOnClickListener { // <-- Establece un OnClickListener para el botón
            // Código a ejecutar cuando se pulse el botón "Acceso"
            val intent = android.content.Intent(this, AccesoActivity::class.java) // <-- Crea un Intent para iniciar AccesoActivity
            startActivity(intent) // <-- Inicia la AccesoActivity
        }
    }
}