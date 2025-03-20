package com.tfc.fat13

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class AccesoActivity : AppCompatActivity() {

    private lateinit var lottieEsperandoCamara: LottieAnimationView
    private lateinit var textoEsperandoCamara: TextView
    private lateinit var videoStreamView: ImageView
    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acceso)

        lottieEsperandoCamara = findViewById(R.id.lottie_esperando_camara)
        textoEsperandoCamara = findViewById(R.id.texto_esperando_camara)
        videoStreamView = findViewById(R.id.video_stream_view)

        Handler(Looper.getMainLooper()).postDelayed({
            Log.d("AccesoActivity", "Entrando en postDelayed")
            lottieEsperandoCamara.visibility = View.GONE
            textoEsperandoCamara.visibility = View.GONE
            videoStreamView.visibility = View.VISIBLE
            startMJpegStream("http://192.168.1.163/stream")
        }, 4000)
    }

    override fun onPause() {
        super.onPause()
        stopMJpegStream()
    }

    override fun onResume() {
        super.onResume()
        if (!running) {
            startMJpegStream("http://192.168.1.163/stream")
        }
    }

    private fun startMJpegStream(url: String) {
        running = true
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val mUrl = URL(url)
                val connection = mUrl.openConnection() as HttpURLConnection
                connection.connect()

                val inputStream = connection.inputStream
                val buffer = ByteArray(1024)
                var bytes: Int = 0
                var byteArrayOutputStream = ByteArrayOutputStream()

                while (running && inputStream.read(buffer).also { bytes = it } != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytes)

                    if (byteArrayOutputStream.toString().contains("\r\n\r\n")) {
                        val bitmap =
                            BitmapFactory.decodeStream(byteArrayOutputStream.toByteArray().inputStream())
                        byteArrayOutputStream.reset()

                        withContext(Dispatchers.Main) {
                            if (bitmap != null && running) {
                                videoStreamView.setImageBitmap(bitmap)
                                videoStreamView.rotation = -90f
                            }
                        }
                    }
                }
                connection.disconnect()
                handler.postDelayed({
                    if (running) {
                        startMJpegStream(url)
                    }
                }, 0)
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("AccesoActivity", "Error en el stream: ${e.message}")
            }

        }
    }

    private fun stopMJpegStream() {
        running = false
    }
}