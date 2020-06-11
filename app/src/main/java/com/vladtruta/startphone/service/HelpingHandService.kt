package com.vladtruta.startphone.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.view.View.OnTouchListener
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import com.vladtruta.startphone.R
import com.vladtruta.startphone.databinding.OverlayHelpingHandBinding
import com.vladtruta.startphone.util.getSize
import org.koin.android.ext.android.inject
import kotlin.math.abs
import kotlin.math.roundToInt

class HelpingHandService : Service(), OnTouchListener, OnGlobalLayoutListener {

    private val windowManager by inject<WindowManager>()
    private lateinit var binding: OverlayHelpingHandBinding
    private lateinit var params: WindowManager.LayoutParams

    private var screenWidth = 0

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @Suppress("DEPRECATION")
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        setTheme(R.style.AppTheme)
        binding = OverlayHelpingHandBinding.inflate(LayoutInflater.from(this), null, false)

        params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        }.apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 100
        }

        windowManager.addView(binding.root, params)

        binding.root.viewTreeObserver.addOnGlobalLayoutListener(this)
        binding.root.setOnTouchListener(this)
    }

    override fun onGlobalLayout() {
        binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)

        val windowSize = windowManager.getSize()
        val width = binding.root.measuredWidth

        //To get the accurate middle of the screen we subtract the width of the floating widget.
        this@HelpingHandService.screenWidth = windowSize.x - width
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                // Remember the initial position.
                initialX = params.x
                initialY = params.y

                // Get the touch location
                initialTouchX = event.rawX
                initialTouchY = event.rawY

                return true
            }
            MotionEvent.ACTION_UP -> {
                val xDiff = event.rawX - initialTouchX
                val yDiff = event.rawY - initialTouchY

                if (abs(xDiff) < 5 && abs(yDiff) < 5) {
                    // Open Helping Hand Dialog
                }

                // Logic to auto-position the widget based on where it is positioned currently w.r.t middle of the screen.
                val middle = screenWidth / 2
                val nearestXWall = (if (params.x >= middle) {
                    screenWidth
                } else {
                    0
                }).toFloat()
                params.x = nearestXWall.toInt()
                windowManager.updateViewLayout(binding.root, params)

                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val xDiff2 = (event.rawX - initialTouchX).roundToInt()
                val yDiff2 = (event.rawY - initialTouchY).roundToInt()

                // Calculate the X and Y coordinates of the view.
                params.x = initialX + xDiff2
                params.y = initialY + yDiff2

                // Update the layout with new X & Y coordinates
                windowManager.updateViewLayout(binding.root, params)

                return true
            }
        }

        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(binding.root)
    }
}