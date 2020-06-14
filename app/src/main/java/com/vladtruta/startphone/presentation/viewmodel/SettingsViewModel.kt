package com.vladtruta.startphone.presentation.viewmodel

import androidx.lifecycle.*
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
        val allApsWithVisibility = allApps.map {
            VisibleApplication(
                it,
                visibleApps.any { visibleApp -> visibleApp.packageName == it.packageName })
        }.sortedBy { !it.isVisible }

        emit(allApsWithVisibility)
    }
    val visibleApplications: LiveData<List<VisibleApplication>> = _visibleApplications

    fun updateAppVisibility(packageName: String, visibility: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            val apps = _visibleApplications.value

            val app =  apps?.firstOrNull { it.applicationInfo.packageName == packageName }
            app?.isVisible = visibility

            apps?.filter { it.isVisible }
                ?.map { it.applicationInfo.packageName }
                ?.let { preferencesHelper.saveVisibleApplications(it) }
        }
    }

    fun areAllAppsHidden(): Boolean {
        return _visibleApplications.value?.all { !it.isVisible } ?: true
    }

    fun signOut() {
        preferencesHelper.clearAuthorizationToken()
        preferencesHelper.clearUserEmail()
        preferencesHelper.clearVisibleApplications()
        preferencesHelper.saveHelpingHandEnabled(false)
    }
}