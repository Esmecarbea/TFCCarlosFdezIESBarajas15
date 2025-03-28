package com.tfc.fat14

import android.content.Context
import org.opencv.android.OpenCVLoader
@Suppress("UNUSED_PARAMETER")
object OpenCVLoader {
    fun initOpenCV(context: Context) {
        OpenCVLoader.initLocal()
    }
}