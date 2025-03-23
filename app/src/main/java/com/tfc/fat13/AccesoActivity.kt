package com.tfc.fat13

import android.graphics.Bitmap
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
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import androidx.core.graphics.createBitmap //Añade esta linea
import java.net.URL

class AccesoActivity : AppCompatActivity() {

    private lateinit var lottieEsperandoCamara: LottieAnimationView
    private lateinit var textoEsperandoCamara: TextView
    private lateinit var videoStreamView: ImageView
    private val handler = Handler(Looper.getMainLooper())
    private var running = false
    private lateinit var mJavaDetector: CascadeClassifier
    private var absoluteFaceSize = 0

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
        // Cargar el clasificador Haar Cascade
        loadClassifier()
    }

    private fun loadClassifier() {
        try {
            val inputStream: InputStream = resources.openRawResource(R.raw.haarcascade_frontalface_alt)
            val cascadeDir: File = getDir("cascade", MODE_PRIVATE)
            val mCascadeFile = File(cascadeDir, "haarcascade_frontalface_alt.xml")
            val os = FileOutputStream(mCascadeFile)

            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                os.write(buffer, 0, bytesRead)
            }
            inputStream.close()
            os.close()

            mJavaDetector = CascadeClassifier(mCascadeFile.absolutePath)
            if (mJavaDetector.empty()) {
                Log.e("AccesoActivity", "Failed to load cascade classifier")
                mJavaDetector = CascadeClassifier()
            } else {
                Log.i("AccesoActivity", "Loaded cascade classifier from $mCascadeFile")
            }
            cascadeDir.delete()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("AccesoActivity", "Failed to load cascade. Exception thrown: $e")
        }
    }

    private fun detectFaces(bitmap: Bitmap): Bitmap {
        Log.d("AccesoActivity", "detectFaces()")
        val imageMat = Mat()
        val grayMat = Mat()
        val faces = MatOfRect()
        Log.d("AccesoActivity", "Antes Utils.bitmapToMat")
        // Convertir el Bitmap a un Mat
        Utils.bitmapToMat(bitmap, imageMat)
        Log.d("AccesoActivity", "Despues Utils.bitmapToMat")

        // Convertir la imagen a escala de grises
        Log.d("AccesoActivity", "Antes de Imgproc.cvtColor")
        Imgproc.cvtColor(imageMat, grayMat, Imgproc.COLOR_BGR2GRAY)
        Log.d("AccesoActivity", "Despues de Imgproc.cvtColor")
        // Calcular tamaño minimo para la detección
        absoluteFaceSize = (grayMat.rows() * 0.2).toInt()
        Log.d("AccesoActivity", "absoluteFaceSize = $absoluteFaceSize")
        // Detectar caras
        Log.d("AccesoActivity", "Antes de mJavaDetector.detectMultiScale")
        mJavaDetector.detectMultiScale(
            grayMat,
            faces,
            1.1,
            2,
            0,
            org.opencv.core.Size(absoluteFaceSize.toDouble(), absoluteFaceSize.toDouble()),
            org.opencv.core.Size()
        )
        Log.d("AccesoActivity", "Despues de mJavaDetector.detectMultiScale")
        Log.d("AccesoActivity", "Hay " + faces.toArray().size + " caras")
        // Dibujar rectángulos alrededor de las caras
        val facesArray = faces.toArray()
        for (i in facesArray.indices) {
            Imgproc.rectangle(
                imageMat,
                facesArray[i].tl(),
                facesArray[i].br(),
                Scalar(0.0, 255.0, 0.0),
                3
            )
        }
        // Convertir el Mat de vuelta a Bitmap
        val resultBitmap = createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(imageMat, resultBitmap)
        Log.d("AccesoActivity", "Fin detectFaces")

        return resultBitmap
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
                var bytes = 0
                val byteArrayOutputStream = ByteArrayOutputStream()

                while (running && inputStream.read(buffer).also { bytes = it } != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytes)

                    if (byteArrayOutputStream.toString().contains("\r\n\r\n")) {

                        val bitmap =
                            BitmapFactory.decodeStream(byteArrayOutputStream.toByteArray().inputStream())

                        byteArrayOutputStream.reset()
                        var bitmapConRostro: Bitmap? = null

                        if (bitmap != null) {
                            bitmapConRostro = detectFaces(bitmap)
                        }

                        withContext(Dispatchers.Main) {
                            if (bitmapConRostro != null && running) {

                                videoStreamView.setImageBitmap(bitmapConRostro)
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