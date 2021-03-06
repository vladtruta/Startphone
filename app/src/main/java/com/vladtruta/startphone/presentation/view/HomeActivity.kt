package com.vladtruta.startphone.presentation.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.vladtruta.startphone.BuildConfig
import com.vladtruta.startphone.R
import com.vladtruta.startphone.databinding.ActivityHomeBinding
import com.vladtruta.startphone.model.local.ApplicationInfo
import com.vladtruta.startphone.presentation.adapter.ApplicationPageAdapter
import com.vladtruta.startphone.presentation.viewmodel.HomeViewModel
import com.vladtruta.startphone.util.ImageHelper
import com.vladtruta.startphone.util.LauncherApplicationsHelper
import com.vladtruta.startphone.util.UIUtils
import com.vladtruta.startphone.widgets.CustomClicksTouchListener
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt


class HomeActivity : BaseActivity(), ApplicationPageAdapter.ApplicationPageListener {
    companion object {
        private const val TAG = "HomeActivity"

        private const val LONG_CLICK_SETTINGS_DELAY_MS = 10000L // 10 Seconds
        private const val RC_SETTINGS = 451
    }

    private val launcherViewModel by viewModel<HomeViewModel>()

    private val imageHelper by inject<ImageHelper>()
    private val launcherApplicationsHelper by inject<LauncherApplicationsHelper>()

    private lateinit var binding: ActivityHomeBinding
    private lateinit var applicationPageAdapter: ApplicationPageAdapter

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RC_SETTINGS -> {
                if (resultCode == Activity.RESULT_OK) {
                    launcherViewModel.refreshVisibleApps()
                    initViewPager()
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewPager()
        initHelpingHandOverlay()
        initActions()
        initObservers()

        launcherViewModel.refreshVisibleApps()
    }

    private fun initViewPager() {
        applicationPageAdapter = ApplicationPageAdapter().apply {
            listener = this@HomeActivity
        }
        binding.applicationsVp.adapter = applicationPageAdapter

        // Disable scrolling of the ViewPager
        binding.applicationsVp.isUserInputEnabled = false

        binding.applicationsVp.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == RecyclerView.NO_POSITION) {
                    return
                }

                val currentPosition = position + 1
                val lastPosition = applicationPageAdapter.itemCount

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

    private fun initHelpingHandOverlay() {
        binding.helpingHandOverlayIncl.helpIv.visibility = View.GONE
        binding.helpingHandOverlayIncl.helpingHandOverlayView.visibility = View.VISIBLE
        binding.helpingHandOverlayIncl.helpingHandLl.visibility = View.VISIBLE
        binding.helpingHandOverlayIncl.closeHelpingHandEfab.visibility = View.VISIBLE
        binding.helpingHandOverlayIncl.closeCurrentAppLl.visibility = View.GONE
        binding.helpingHandOverlayIncl.stuckLl.visibility = View.GONE
        binding.helpingHandOverlayIncl.tutorialPagesLl.visibility = View.GONE

        binding.helpingHandOverlayIncl.closeHelpingHandEfab.setOnClickListener { hideHelpingHandOverlay() }
    }

    private fun initActions() {
        binding.previousPageEfab.setOnClickListener {
            binding.applicationsVp.setCurrentItem(binding.applicationsVp.currentItem - 1, true)
        }

        binding.nextPageEfab.setOnClickListener {
            binding.applicationsVp.setCurrentItem(binding.applicationsVp.currentItem + 1, true)
        }

        binding.timeIncl.root.setOnTouchListener(object :
            CustomClicksTouchListener(LONG_CLICK_SETTINGS_DELAY_MS) {
            override fun onLongClick() {
                launchSettings()
            }

            override fun onClick() {
                // Only a short tap, do nothing
            }
        })
    }

    private fun initObservers() {
        launcherViewModel.batteryLevel.observe(this, Observer {
            it ?: return@Observer

            val percentage = it.first
            val isCharging = it.second

            val batteryImage: Int
            val batteryColor: Int

            when (percentage) {
                in 0..30 -> {
                    batteryImage = R.drawable.ic_battery_1
                    batteryColor = android.R.color.holo_red_dark
                }
                in 31..70 -> {
                    batteryImage = R.drawable.ic_battery_2
                    batteryColor = android.R.color.holo_orange_dark
                }
                in 71..100 -> {
                    batteryImage = R.drawable.ic_battery_3
                    batteryColor = android.R.color.holo_green_dark
                }
                else -> {
                    batteryImage = R.drawable.ic_battery_0
                    batteryColor = android.R.color.holo_green_dark
                }
            }

            binding.batteryIncl.batteryLevelTv.text =
                UIUtils.getString(R.string.battery_percentage_placeholder, percentage)
            binding.batteryIncl.batteryLevelIv.setImageResource(batteryImage)
            binding.batteryIncl.batteryLevelIv.imageTintList =
                UIUtils.getColorStateList(batteryColor)

            binding.batteryIncl.batteryChargingIv.visibility = if (isCharging) {
                View.VISIBLE
            } else {
                View.GONE
            }
        })

        launcherViewModel.currentWeather.observe(this, Observer { result ->
            if (result == null) {
                return@Observer
            }

            result.onSuccess {
                binding.weatherIncl.weatherLl.visibility = View.VISIBLE
                binding.weatherIncl.loadingPb.visibility = View.GONE

                binding.weatherIncl.weatherTv.text = it.main
                binding.weatherIncl.temperatureTv.text = UIUtils.getString(
                    R.string.temperature_degrees_placeholder,
                    it.temperature.roundToInt()
                )

                val imageUrl = "${BuildConfig.OPEN_WEATHER_ICON_BASE_URL}${it.iconUrl}@4x.png"
                imageHelper.loadImage(this, binding.weatherIncl.weatherIv, imageUrl)
            }.onFailure {
                binding.weatherIncl.weatherLl.visibility = View.INVISIBLE
                binding.weatherIncl.loadingPb.visibility = View.VISIBLE
            }
        })

        launcherViewModel.networkConnected.observe(this, Observer {
            it ?: return@Observer

            if (!it) {
                val noInternetImage = R.drawable.ic_wifi_0
                val noInternetColor = android.R.color.holo_red_dark
                val noInternetLevel = 0

                binding.internetIncl.internetLevelIv.setImageResource(noInternetImage)
                binding.internetIncl.internetLevelIv.imageTintList =
                    UIUtils.getColorStateList(noInternetColor)
                binding.internetIncl.internetLevelTv.text = noInternetLevel.toString()
            }
        })

        launcherViewModel.networkConnectionStrength.observe(this, Observer {
            it ?: return@Observer

            binding.internetIncl.internetLevelTv.text = it.toString()

            val levelImage: Int
            val levelColor: Int
            val levelNumber: Int

            when (it) {
                1 -> {
                    levelNumber = 1
                    levelImage = R.drawable.ic_wifi_1
                    levelColor = android.R.color.holo_red_dark
                }
                in 2..3 -> {
                    levelNumber = 2
                    levelImage = R.drawable.ic_wifi_2
                    levelColor = android.R.color.holo_orange_dark
                }
                4 -> {
                    levelNumber = 3
                    levelImage = R.drawable.ic_wifi_3
                    levelColor = android.R.color.holo_green_dark
                }
                else -> {
                    levelNumber = 0
                    levelImage = R.drawable.ic_wifi_0
                    levelColor = android.R.color.holo_red_dark
                }
            }

            binding.internetIncl.internetLevelTv.text = levelNumber.toString()
            binding.internetIncl.internetLevelIv.setImageResource(levelImage)
            binding.internetIncl.internetLevelIv.imageTintList =
                UIUtils.getColorStateList(levelColor)
        })

        launcherViewModel.mobileSignalStrength.observe(this, Observer {
            it ?: return@Observer

            val signalImage: Int
            val signalColor: Int
            val signalNumber: Int

            when (it) {
                1 -> {
                    signalNumber = 1
                    signalImage = R.drawable.ic_signal_1
                    signalColor = android.R.color.holo_red_dark
                }
                in 2..3 -> {
                    signalNumber = 2
                    signalImage = R.drawable.ic_signal_2
                    signalColor = android.R.color.holo_orange_dark
                }
                4 -> {
                    signalNumber = 3
                    signalImage = R.drawable.ic_signal_3
                    signalColor = android.R.color.holo_green_dark
                }
                else -> {
                    signalNumber = 0
                    signalImage = R.drawable.ic_signal_0
                    signalColor = android.R.color.holo_red_dark
                }
            }

            binding.signalIncl.signalStrengthTv.text = signalNumber.toString()
            binding.signalIncl.signalStrengthIv.setImageResource(signalImage)
            binding.signalIncl.signalStrengthIv.imageTintList =
                UIUtils.getColorStateList(signalColor)
        })

        launcherViewModel.dateTime.observe(this, Observer {
            it ?: return@Observer

            binding.timeIncl.timeTv.text = it.time
            binding.calendarIncl.dayOfWeekTv.text = it.dayOfWeek
            binding.calendarIncl.dayOfMonthTv.text = it.dayOfMonth
        })

        launcherViewModel.visibleAppLists.observe(this, Observer {
            it ?: return@Observer

            applicationPageAdapter.submitList(it)
        })
    }

    override fun onApplicationClicked(applicationInfo: ApplicationInfo) {
        Log.d(TAG, "onApplicationClicked: $applicationInfo")

        launcherViewModel.retrieveTutorialsForPackageName(applicationInfo.packageName)
        launcherApplicationsHelper.updateCurrentlyRunningApplication(applicationInfo)
        launcherApplicationsHelper.startApplicationByPackageName(this, applicationInfo.packageName)
    }

    override fun onNeedHelpClicked() {
        showHelpingHandOverlay()
    }

    private fun showHelpingHandOverlay() {
        binding.helpingHandOverlayIncl.root.visibility = View.VISIBLE
    }

    private fun hideHelpingHandOverlay() {
        binding.helpingHandOverlayIncl.root.visibility = View.GONE
    }

    private fun launchSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivityForResult(intent, RC_SETTINGS)
    }
}
