package com.tfc.fat13

import android.content.Context
import android.util.Log
import org.opencv.android.OpenCVLoader

object OpenCVLoader {
    private const val TAG = "OpenCVLoader"

    fun initOpenCV(context: Context) {
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV initialized successfully")
        } else {
            Log.e(TAG, "OpenCV initialization failed")
        }
    }
}