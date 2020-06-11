package com.vladtruta.startphone.util

import android.graphics.Point
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import com.vladtruta.startphone.R

fun WindowManager.getSize(): Point {
    val size = Point()
    defaultDisplay.getSize(size)

    return size
}

fun View.setRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(R.attr.selectableItemBackground, this, true)
    setBackgroundResource(resourceId)
}

fun View.setCircleRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(R.attr.selectableItemBackgroundBorderless, this, true)
    setBackgroundResource(resourceId)
}