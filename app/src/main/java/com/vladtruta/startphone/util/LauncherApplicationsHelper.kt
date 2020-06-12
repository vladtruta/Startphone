package com.vladtruta.startphone.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vladtruta.startphone.model.local.ApplicationInfo
import com.vladtruta.startphone.presentation.view.LauncherActivity


class LauncherApplicationsHelper(private val packageManager: PackageManager) {

    private val _currentlyRunningApplication = MutableLiveData<ApplicationInfo>()
    val currentlyRunningApplication: LiveData<ApplicationInfo> = _currentlyRunningApplication

    fun getApplicationInfoForAllApps(): List<ApplicationInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return packageManager.queryIntentActivities(intent, 0)
            .map {
                ApplicationInfo(
                    it.loadLabel(packageManager).toString(),
                    it.activityInfo.packageName,
                    it.activityInfo.loadIcon(packageManager)
                )
            }
    }

    fun getApplicationInfoForPackageNames(vararg packageNames: String): List<ApplicationInfo> {
        return getApplicationInfoForAllApps()
            .filter { app -> packageNames.any { app.packageName == it } }
    }

    fun updateCurrentlyRunningApplication(applicationInfo: ApplicationInfo) {
        _currentlyRunningApplication.postValue(applicationInfo)
    }

    fun restartCurrentlyRunningApplication(context: Context) {
        currentlyRunningApplication.value?.packageName?.let {
            return startApplicationByPackageName(context, it)
        } ?: startLauncher(context)
    }

    fun startApplicationByPackageName(context: Context, packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        intent?.let { context.startActivity(intent) } ?: startLauncher(context)
    }

    fun startLauncher(context: Context) {
        val intent = Intent(context, LauncherActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(intent)
    }
}