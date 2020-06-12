package com.vladtruta.startphone.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.vladtruta.startphone.model.local.ApplicationInfo
import com.vladtruta.startphone.presentation.view.LauncherActivity


class LauncherApplicationsHelper(private val packageManager: PackageManager) {

    var currentlyRunningApplication: ApplicationInfo? = null
        private set

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
        currentlyRunningApplication = applicationInfo
    }

    fun restartCurrentlyRunningApplication(context: Context) {
        currentlyRunningApplication?.packageName?.let {
            return restartApplicationByPackageName(context, it)
        } ?: return
    }

    fun restartApplicationByPackageName(context: Context, packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        } ?: return

        context.startActivity(intent)
    }

    fun startLauncher(context: Context) {
        val intent = Intent(context, LauncherActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(intent)
    }
}