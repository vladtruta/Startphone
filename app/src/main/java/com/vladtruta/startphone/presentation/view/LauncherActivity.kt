package com.vladtruta.startphone.presentation.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import com.vladtruta.startphone.R
import com.vladtruta.startphone.presentation.viewmodel.LauncherViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class LauncherActivity : AppCompatActivity() {

    private val launcherViewModel by viewModel<LauncherViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        initObservers()
    }

    private fun initObservers() {
        launcherViewModel.batteryLevel.observe(this, Observer { })

        launcherViewModel.currentWeather.observe(this, Observer { })

        launcherViewModel.wifiSignalLevel.observe(this, Observer { })

        launcherViewModel.mobileSignalStrength.observe(this, Observer { })

        launcherViewModel.dateTime.observe(this, Observer { })
    }
}
