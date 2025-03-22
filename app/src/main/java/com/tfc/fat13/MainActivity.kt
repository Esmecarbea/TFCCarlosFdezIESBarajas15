package com.tfc.fat13

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        OpenCVLoader.initOpenCV(this)

        val botonAcceso: MaterialButton = findViewById(R.id.boton_acceso)
        val botonModoTaller: MaterialButton = findViewById(R.id.boton_modo_taller)

        botonAcceso.setOnClickListener {
            val intent = Intent(this, AccesoActivity::class.java)
            startActivity(intent)
        }

        botonModoTaller.setOnClickListener {
            val intent = Intent(this, ModoTallerActivity::class.java)
            startActivity(intent)
        }
    }
}