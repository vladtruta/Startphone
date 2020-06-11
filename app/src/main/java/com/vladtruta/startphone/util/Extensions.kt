package com.vladtruta.startphone.util

import android.graphics.Point
import android.view.WindowManager

fun WindowManager.getSize(): Point {
    val size = Point()
    defaultDisplay.getSize(size)

    return size
}