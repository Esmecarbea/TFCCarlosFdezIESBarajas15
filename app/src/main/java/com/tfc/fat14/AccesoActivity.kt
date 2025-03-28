package com.tfc.fat14

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import com.airbnb.lottie.LottieAnimationView
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class AccesoActivity : AppCompatActivity() {

    private lateinit var lottieEsperandoCamara: LottieAnimationView
    private lateinit var textoEsperandoCamara: TextView
    private lateinit var videoStreamView: ImageView
    private lateinit var mJavaDetector: CascadeClassifier
    private var absoluteFaceSize = 0
    private lateinit var webSocketManager: WebSocketManager

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
        }, 4000)

        // Cargar el clasificador Haar Cascade
        loadClassifier()
        // Inicializar WebSocketManager
        val myWebSocketListener = MyWebSocketListener() // Clase interna.
        webSocketManager = WebSocketManager(myWebSocketListener) //Instancia
        webSocketManager.connect() // Conectar
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
        Utils.bitmapToMat(bitmap, imageMat)
        Imgproc.cvtColor(imageMat, grayMat, Imgproc.COLOR_BGR2GRAY)
        absoluteFaceSize = (grayMat.rows() * 0.2).toInt()
        mJavaDetector.detectMultiScale(
            grayMat,
            faces,
            1.1,
            2,
            0,
            org.opencv.core.Size(absoluteFaceSize.toDouble(), absoluteFaceSize.toDouble()),
            org.opencv.core.Size()
        )
        Log.d("AccesoActivity", "Hay " + faces.toArray().size + " caras")
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
        val resultBitmap = createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(imageMat, resultBitmap)
        Log.d("AccesoActivity", "Fin detectFaces")

        return resultBitmap
    }
    //Clase Interna
    inner class MyWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            Log.d("AccesoActivity", "Conexion onOpen")
            //TODO: Enviar mensaje inicial.
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            Log.d("AccesoActivity", "Conexion onMessage: $text")
            //TODO: Recibir datos JSON.
        }
        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
            Log.d("AccesoActivity", "Cerrando conexión: $code $reason")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            Log.d("AccesoActivity", "Conexión cerrada: $code $reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            Log.e("AccesoActivity", "Error: ${t.message}")
            t.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketManager.close()
    }

    override fun onPause() {
        super.onPause()
        webSocketManager.close()
    }
}