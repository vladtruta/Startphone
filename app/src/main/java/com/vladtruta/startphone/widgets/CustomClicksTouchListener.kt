package com.vladtruta.startphone.widgets

import android.graphics.Rect
import android.os.Handler
import android.view.MotionEvent
import android.view.View

abstract class CustomClicksTouchListener(private val longClickDelayMs: Long) : View.OnTouchListener {
    private var viewRect = Rect(0, 0, 0, 0)
    private val handler = Handler()
    private var isLongPressing = false

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            viewRect = Rect(view.left, view.top, view.right, view.bottom)
            isLongPressing = true

            handler.postDelayed({
                if (isLongPressing) {
                    onLongClick()

                    isLongPressing = false
                }
            }, longClickDelayMs)
        } else if (event.action == MotionEvent.ACTION_UP) {
            if (isLongPressing) {
                if (viewRect.contains(view.left + event.x.toInt(), view.top + event.y.toInt())) {
                    onClick()
                }

                handler.removeCallbacksAndMessages(null)
            }

            isLongPressing = false
        }

        return true
    }

    abstract fun onClick()
    abstract fun onLongClick()
}