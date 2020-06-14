package com.vladtruta.startphone.service

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.LauncherActivity
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.vladtruta.startphone.R
import com.vladtruta.startphone.databinding.ServiceHelpingHandBinding
import com.vladtruta.startphone.model.local.ApplicationInfo
import com.vladtruta.startphone.model.local.Tutorial
import com.vladtruta.startphone.presentation.adapter.TutorialPageAdapter
import com.vladtruta.startphone.repository.IAppRepo
import com.vladtruta.startphone.util.LauncherApplicationsHelper
import com.vladtruta.startphone.util.NotificationsHelper
import com.vladtruta.startphone.util.UIUtils
import com.vladtruta.startphone.util.getSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.math.abs
import kotlin.math.roundToInt


class HelpingHandService : LifecycleService(), OnTouchListener, OnGlobalLayoutListener,
    TutorialPageAdapter.TutorialPageListener {

    companion object {
        private const val TAG = "HelpingHandService"

        private const val ANIMATE_TO_NEAREST_WALL_DURATION_MS = 300L

        private const val STARTPHONE_FOREGROUND_SERVICE_ID = 731
        private const val NOTIFICATION_CHANNEL_STARTPHONE_OVERLAY =
            "NOTIFICATION_CHANNEL_HELPING_HAND"

        private const val BUBBLE_INITIAL_X_POSITION = 0
        private val BUBBLE_INITIAL_Y_POSITION = UIUtils.dpToPx(60f)

        private const val MAX_TUTORIALS_PER_PAGE = 5
    }

    private val windowManager by inject<WindowManager>()
    private val notificationsHelper by inject<NotificationsHelper>()
    private val launcherApplicationsHelper by inject<LauncherApplicationsHelper>()
    private val applicationRepository by inject<IAppRepo>()

    private lateinit var binding: ServiceHelpingHandBinding
    private lateinit var params: WindowManager.LayoutParams

    private lateinit var tutorialPageAdapter: TutorialPageAdapter

    private var screenWidth = 0

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private var userAlreadyRated = false

    private val job = SupervisorJob()

    @Suppress("DEPRECATION")
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        setTheme(R.style.AppTheme)
        binding = ServiceHelpingHandBinding.inflate(LayoutInflater.from(this), null, false)

        updateWindowParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        windowManager.addView(binding.root, params)

        binding.root.viewTreeObserver.addOnGlobalLayoutListener(this)
        binding.root.setOnTouchListener(this)

        initViewPager()
        initObservers()
        initActions()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startServiceInForeground()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        windowManager.removeView(binding.root)
        job.cancel()

        super.onDestroy()
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
                PendingIntent.getActivity(
                    this,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationsHelper.createNotificationChannel(
                NOTIFICATION_CHANNEL_STARTPHONE_OVERLAY,
                UIUtils.getString(R.string.app_name),
                UIUtils.getString(R.string.notification_channel_description),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        }

        val notification = notificationsHelper.createNotification(
            this,
            NOTIFICATION_CHANNEL_STARTPHONE_OVERLAY,
            UIUtils.getString(R.string.app_name),
            UIUtils.getString(R.string.foreground_notification_content_text),
            R.drawable.ic_help_outline,
            pendingIntent
        )

        startForeground(STARTPHONE_FOREGROUND_SERVICE_ID, notification)
    }

    private fun initViewPager() {
        tutorialPageAdapter = TutorialPageAdapter().apply {
            listener = this@HelpingHandService
        }
        binding.tutorialsVp.adapter = tutorialPageAdapter

        // Disable scrolling of the ViewPager
        binding.tutorialsVp.isUserInputEnabled = false

        binding.tutorialsVp.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == RecyclerView.NO_POSITION) {
                    return
                }

                val currentPosition = position + 1
                val lastPosition = tutorialPageAdapter.itemCount

                when (currentPosition) {
                    1 -> {
                        binding.previousPageEfab.visibility = View.INVISIBLE
                        if (lastPosition == 1) {
                            binding.nextPageEfab.visibility = View.INVISIBLE
                        } else {
                            binding.nextPageEfab.visibility = View.VISIBLE
                        }
                    }
                    lastPosition -> {
                        binding.previousPageEfab.visibility = View.VISIBLE
                        binding.nextPageEfab.visibility = View.INVISIBLE
                    }
                    else -> {
                        binding.previousPageEfab.visibility = View.VISIBLE
                        binding.nextPageEfab.visibility = View.VISIBLE
                    }
                }

                binding.currentPageTv.text = UIUtils.getString(
                    R.string.current_page_placeholder,
                    currentPosition,
                    lastPosition
                )
                binding.previousPageEfab.text =
                    UIUtils.getString(R.string.page_placeholder, currentPosition - 1)
                binding.nextPageEfab.text =
                    UIUtils.getString(R.string.page_placeholder, currentPosition + 1)
            }
        })
    }

    private fun initObservers() {
        launcherApplicationsHelper.currentlyRunningApplication.observe(this, Observer {
            updateDialogWithCurrentlyRunningApplication(it)
        })

        launcherApplicationsHelper.tutorialsForCurrentlyRunningApplication.observe(this, Observer {
            it ?: return@Observer

            if (it.isEmpty()) {
                binding.tutorialPagesLl.visibility = View.GONE
                sendTutorialMissingRequest()
            } else {
                binding.tutorialPagesLl.visibility = View.VISIBLE
            }

            tutorialPageAdapter.submitList(it.chunked(MAX_TUTORIALS_PER_PAGE))
        })
    }

    override fun onTutorialClicked(tutorial: Tutorial) {
        userAlreadyRated = false

        openVideoOverlay()
        initTutorialOverlay(tutorial)
    }

    private fun initActions() {
        binding.closeHelpingHandEfab.setOnClickListener {
            closeHelpingHandDialog()
            closeVideoOverlay()
        }

        binding.closeCurrentAppLl.setOnClickListener {
            launcherApplicationsHelper.openLauncher(this)
        }

        binding.stuckLl.setOnClickListener {
            launcherApplicationsHelper.restartCurrentlyRunningApplication(this)
            closeHelpingHandDialog()
        }
    }

    private fun openHelpingHandDialog() {
        updateWindowParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        windowManager.updateViewLayout(binding.root, params)

        binding.helpIv.visibility = View.GONE
        binding.helpingHandOverlayView.visibility = View.VISIBLE
        binding.closeHelpingHandEfab.visibility = View.VISIBLE
        binding.helpingHandLl.visibility = View.VISIBLE
    }

    private fun updateDialogWithCurrentlyRunningApplication(currentlyRunningApplication: ApplicationInfo?) {
        binding.closeCurrentAppTv.text = UIUtils.getString(
            R.string.exit_app_placeholder,
            currentlyRunningApplication?.label ?: UIUtils.getString(R.string.this_application)
        )
        binding.closeAppIv.setImageDrawable(currentlyRunningApplication?.icon)
    }

    private fun closeHelpingHandDialog() {
        updateWindowParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        windowManager.updateViewLayout(binding.root, params)

        binding.helpIv.visibility = View.VISIBLE
        binding.helpingHandOverlayView.visibility = View.GONE
        binding.closeHelpingHandEfab.visibility = View.GONE
        binding.helpingHandLl.visibility = View.GONE
    }

    private fun openVideoOverlay() {
        binding.closeCurrentAppLl.visibility = View.GONE
        binding.stuckLl.visibility = View.GONE
        binding.tutorialPagesLl.visibility = View.GONE
        binding.tutorialsVp.visibility = View.GONE

        binding.tutorialFl.visibility = View.VISIBLE
        binding.loadingVideoPb.visibility = View.VISIBLE

        binding.usefulLl.visibility = View.GONE
        binding.watchAgainLl.visibility = View.GONE
    }

    private fun closeVideoOverlay() {
        binding.closeCurrentAppLl.visibility = View.VISIBLE
        binding.stuckLl.visibility = View.VISIBLE
        binding.tutorialPagesLl.visibility =
            if (launcherApplicationsHelper.tutorialsForCurrentlyRunningApplication.value.isNullOrEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }
        binding.tutorialsVp.visibility = View.VISIBLE

        binding.tutorialFl.visibility = View.GONE
        binding.loadingVideoPb.visibility = View.GONE

        binding.usefulLl.visibility = View.GONE
        binding.watchAgainLl.visibility = View.GONE

        binding.tutorialVv.stopPlayback()
    }

    private fun initTutorialOverlay(tutorial: Tutorial) {
        binding.helpTitleTv.text = tutorial.title

        binding.tutorialVv.setVideoURI(Uri.parse(tutorial.videoUrl))
        binding.tutorialVv.start()
        sendTutorialWatchedRequest(tutorial)

        binding.tutorialVv.setOnPreparedListener {
            binding.loadingVideoPb.visibility = View.GONE
        }

        binding.tutorialVv.setOnCompletionListener {
            if (userAlreadyRated) {
                openWatchAgainDialog()
            } else {
                openUsefulDialog()
            }
        }

        binding.tutorialVv.setOnErrorListener { mp, what, extra ->
            Log.e(TAG, "$mp, $what, $extra")

            return@setOnErrorListener false
        }

        binding.usefulNoMb.setOnClickListener {
            closeUsefulDialog()
            openWatchAgainDialog()

            userAlreadyRated = true
            sendTutorialRatedRequest(tutorial, false)
        }

        binding.usefulYesMb.setOnClickListener {
            closeUsefulDialog()
            openWatchAgainDialog()

            userAlreadyRated = true
            sendTutorialRatedRequest(tutorial, true)
        }

        binding.watchAgainNoMb.setOnClickListener {
            closeWatchAgainDialog()
            closeVideoOverlay()
            closeHelpingHandDialog()
        }

        binding.watchAgainYesMb.setOnClickListener {
            closeWatchAgainDialog()

            binding.tutorialVv.start()
            sendTutorialWatchedRequest(tutorial)
        }
    }

    private fun openUsefulDialog() {
        binding.usefulLl.visibility = View.VISIBLE
    }

    private fun closeUsefulDialog() {
        binding.usefulLl.visibility = View.GONE
    }

    private fun openWatchAgainDialog() {
        binding.watchAgainLl.visibility = View.VISIBLE
    }

    private fun closeWatchAgainDialog() {
        binding.watchAgainLl.visibility = View.GONE
    }

    private fun updateWindowParams(width: Int, height: Int) {
        params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                width,
                height,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                width,
                height,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        }.apply {
            gravity = Gravity.TOP or Gravity.START
            x = BUBBLE_INITIAL_X_POSITION
            y = BUBBLE_INITIAL_Y_POSITION
        }
    }

    //region Network Requests
    private fun sendTutorialWatchedRequest(tutorial: Tutorial) {
        CoroutineScope(Dispatchers.Main + job).launch {
            try {
                applicationRepository.updateWatchedTutorial(tutorial.id)
            } catch (e: Exception) {
                Log.e(TAG, "sendTutorialWatchedRequest Failure: ${e.message}", e)
            }
        }
    }

    private fun sendTutorialRatedRequest(tutorial: Tutorial, videoUseful: Boolean) {
        CoroutineScope(Dispatchers.Main + job).launch {
            try {
                applicationRepository.updateRatedTutorial(tutorial.id, videoUseful)
            } catch (e: Exception) {
                Log.e(TAG, "sendTutorialRatedRequest Failure: ${e.message}", e)
            }
        }
    }

    private fun sendTutorialMissingRequest() {
        val packageName =
            launcherApplicationsHelper.currentlyRunningApplication.value?.packageName ?: run {
                Log.e(TAG, "packageName of current app is null")
                return
            }

        CoroutineScope(Dispatchers.Main + job).launch {
            try {
                applicationRepository.updateMissingTutorial(packageName)
            } catch (e: Exception) {
                Log.e(TAG, "sendTutorialMissingRequest Failure: ${e.message}", e)
            }
        }
    }
    //endregion
}