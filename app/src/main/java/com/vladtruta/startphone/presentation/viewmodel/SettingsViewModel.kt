package com.vladtruta.startphone.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.vladtruta.startphone.model.local.VisibleApplication
import com.vladtruta.startphone.repository.IAppRepo
import com.vladtruta.startphone.util.LauncherApplicationsHelper
import com.vladtruta.startphone.util.PreferencesHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesHelper: PreferencesHelper,
    private val launcherApplicationsHelper: LauncherApplicationsHelper,
    private val applicationRepository: IAppRepo
) : ViewModel() {
    companion object {
        private const val TAG = "SettingsViewModel"
    }

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

    private val _updateApplicationRequestSent = MutableLiveData(false)
    val updateApplicationRequestSent: LiveData<Boolean> = _updateApplicationRequestSent

    fun updateAppVisibility(packageName: String, visibility: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            val apps = _visibleApplications.value

            val app = apps?.firstOrNull { it.applicationInfo.packageName == packageName }
            app?.isVisible = visibility

            apps?.filter { it.isVisible }
                ?.map { it.applicationInfo.packageName }
                ?.let { preferencesHelper.saveVisibleApplications(it) }
        }
    }

    fun areAllAppsHidden(): Boolean {
        return _visibleApplications.value?.all { !it.isVisible } ?: true
    }

    fun sendUpdateApplicationsRequest() {
        _updateApplicationRequestSent.postValue(false)
        viewModelScope.launch {
            try {
                val allApplications = launcherApplicationsHelper.getApplicationInfoForAllApps()
                applicationRepository.updateApplications(allApplications)
            } catch (e: Exception) {
                Log.e(TAG, "updateApplications Failure: ${e.message}", e)
            } finally {
                _updateApplicationRequestSent.postValue(true)
            }
        }
    }

    fun signOut() {
        preferencesHelper.clearAuthorizationToken()
        preferencesHelper.clearUserEmail()
        preferencesHelper.clearVisibleApplications()
        preferencesHelper.saveHelpingHandEnabled(false)
    }
}