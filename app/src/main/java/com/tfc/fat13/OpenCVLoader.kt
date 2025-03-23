package com.tfc.fat13

import android.content.Context
import org.opencv.android.OpenCVLoader
@Suppress("UNUSED_PARAMETER")
object OpenCVLoader {
    fun initOpenCV(context: Context) {
        OpenCVLoader.initLocal()
    }
}