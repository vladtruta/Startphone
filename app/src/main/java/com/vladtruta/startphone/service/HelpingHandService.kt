package com.vladtruta.startphone.service

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.*
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.view.View.OnTouchListener
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.core.app.NotificationCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.vladtruta.startphone.R
import com.vladtruta.startphone.databinding.OverlayHelpingHandBinding
import com.vladtruta.startphone.util.UIUtils
import com.vladtruta.startphone.util.getSize
import org.koin.android.ext.android.inject
import kotlin.math.abs
import kotlin.math.roundToInt


class HelpingHandService : Service(), OnTouchListener, OnGlobalLayoutListener {

    companion object {
        private const val ANIMATE_TO_NEAREST_WALL_DURATION_MS = 300L

        private const val STARTPHONE_FOREGROUND_SERVICE_ID = 731
        private const val NOTIFICATION_CHANNEL_STARTPHONE_OVERLAY = "NOTIFICATION_CHANNEL_HELPING_HAND"
    }

    private val windowManager by inject<WindowManager>()
    private val notificationManager by inject<NotificationManager>()

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
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startServiceInForeground()

        return super.onStartCommand(intent, flags, startId)
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
                    openHelpingHandDialog()
                }

                // Logic to auto-position the widget based on where it is positioned currently w.r.t middle of the screen.
                val middle = screenWidth / 2
                val nearestXWall = (if (params.x >= middle) {
                    screenWidth
                } else {
                    0
                }).toFloat()

                ObjectAnimator.ofFloat(params.x.toFloat(), nearestXWall).apply {
                    interpolator = FastOutSlowInInterpolator()
                    duration = ANIMATE_TO_NEAREST_WALL_DURATION_MS
                    addUpdateListener {
                        params.x = (it.animatedValue as? Float ?: 0.0f).roundToInt()
                        windowManager.updateViewLayout(binding.root, params)
                    }
                    start()
                }

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

    private fun startServiceInForeground() {
        val pendingIntent: PendingIntent = Intent(this, LauncherActivity::class.java)
            .let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(
                NOTIFICATION_CHANNEL_STARTPHONE_OVERLAY,
                UIUtils.getString(R.string.app_name),
                UIUtils.getString(R.string.notification_channel_description),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        }

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_STARTPHONE_OVERLAY)
            .setContentTitle(UIUtils.getString(R.string.app_name))
            .setContentText(UIUtils.getString(R.string.foreground_notification_content_text))
            .setSmallIcon(R.drawable.ic_help_outline)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(STARTPHONE_FOREGROUND_SERVICE_ID, notification)
    }

    @TargetApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(
        channelId: String,
        name: String,
        description: String,
        importance: Int,
        groupId: String? = null,
        showBadge: Boolean = true,
        forceDisableSound: Boolean = false,
        forceDisableVibration: Boolean = false
    ) {
        val channel = NotificationChannel(channelId, name, importance).apply {
            this.description = description
            this.lightColor = Color.RED
            this.setShowBadge(showBadge)

            if (forceDisableSound) {
                this.setSound(null, null)
            }

            if (forceDisableVibration) {
                this.enableVibration(false)
            }
        }

        // Set the notification channel groupId (if there is one)
        groupId?.let { channel.group = groupId }

        // Register the channel with the system
        notificationManager.createNotificationChannel(channel)
    }

    private fun openHelpingHandDialog() {
        binding.helpIv.visibility = View.GONE
        binding.helpingHandLl.visibility = View.VISIBLE
        binding.helpingHandOverlayView.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(binding.root)
    }
}