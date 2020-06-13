package com.vladtruta.startphone.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.vladtruta.startphone.BuildConfig
import com.vladtruta.startphone.model.local.VisibleApplication
import com.vladtruta.startphone.util.LauncherApplicationsHelper
import kotlinx.coroutines.Dispatchers

class VisibleAppsViewModel(
    private val launcherApplicationsHelper: LauncherApplicationsHelper
) : ViewModel() {

    val applications = liveData(Dispatchers.Default) {
        val allApps = launcherApplicationsHelper.getApplicationInfoForAllApps()
        val visibleApps = allApps
            .filter { it.packageName != BuildConfig.APPLICATION_ID }
            .map { VisibleApplication(it, false) }
        emit(visibleApps)
    }

}