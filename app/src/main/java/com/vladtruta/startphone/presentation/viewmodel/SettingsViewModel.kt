package com.vladtruta.startphone.presentation.viewmodel

import androidx.lifecycle.*
import com.vladtruta.startphone.model.local.ApplicationInfo
import com.vladtruta.startphone.model.local.VisibleApplication
import com.vladtruta.startphone.util.LauncherApplicationsHelper
import com.vladtruta.startphone.util.PreferencesHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesHelper: PreferencesHelper,
    private val launcherApplicationsHelper: LauncherApplicationsHelper
) : ViewModel() {

    private val _signedInEmail = MutableLiveData<String>(preferencesHelper.getUserEmail())
    val signedInEmail: LiveData<String> = _signedInEmail

    private val _visibleApplications = liveData(Dispatchers.Default) {
        val allApps = launcherApplicationsHelper.getApplicationInfoForAllApps()

        val savedPackages = preferencesHelper.getVisibleApplications().toTypedArray()
        val visibleApps =
            launcherApplicationsHelper.getApplicationInfoForPackageNames(*savedPackages)

        emit(allApps.map { VisibleApplication(it, visibleApps.contains(it)) })
    }
    val visibleApplications: LiveData<List<VisibleApplication>> = _visibleApplications

    fun updateAppVisibility(packageName: String, visibility: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            val apps = _visibleApplications.value

            apps?.firstOrNull { it.applicationInfo.packageName == packageName }
                ?.isVisible = visibility

            apps?.map { it.applicationInfo.packageName }
                ?.let { preferencesHelper.saveVisibleApplications(it) }
        }
    }

    fun getVisibleApplicationsInfo(): List<ApplicationInfo> {
        return _visibleApplications.value?.map { it.applicationInfo } ?: emptyList()
    }

    fun signOut() {
        preferencesHelper.clearAuthorizationToken()
        preferencesHelper.clearUserEmail()
    }
}