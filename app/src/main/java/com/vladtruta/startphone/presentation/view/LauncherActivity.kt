package com.vladtruta.startphone.presentation.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.vladtruta.startphone.BuildConfig
import com.vladtruta.startphone.R
import com.vladtruta.startphone.databinding.ActivityLauncherBinding
import com.vladtruta.startphone.presentation.viewmodel.LauncherViewModel
import com.vladtruta.startphone.util.ImageHelper
import com.vladtruta.startphone.util.UIUtils
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt

class LauncherActivity : AppCompatActivity() {

    private val launcherViewModel by viewModel<LauncherViewModel>()
    private val imageHelper by inject<ImageHelper>()

    private lateinit var binding: ActivityLauncherBinding

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_launcher)

        initObservers()
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

            binding.batteryIncl.batteryLevelTv.text = percentage.toString()
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
                binding.weatherIncl.temperatureTv.text = it.temperature.roundToInt().toString()

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

                binding.internetIncl.internetLevelIv.setImageResource(noInternetImage)
                binding.internetIncl.internetLevelIv.imageTintList =
                    UIUtils.getColorStateList(noInternetColor)
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
    }
}
