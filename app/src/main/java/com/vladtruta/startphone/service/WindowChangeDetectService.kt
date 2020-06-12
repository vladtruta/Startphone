package com.vladtruta.startphone.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.vladtruta.startphone.BuildConfig
import com.vladtruta.startphone.util.LauncherApplicationsHelper
import com.vladtruta.startphone.util.StartphoneApp
import org.koin.android.ext.android.inject

@SuppressLint("LongLogTag")
class WindowChangeDetectService : AccessibilityService() {
    companion object {
        private const val TAG = "WindowChangeDetectService"
    }

    private val launcherApplicationsHelper by inject<LauncherApplicationsHelper>()
    private val startphoneApp by inject<StartphoneApp>()

    override fun onServiceConnected() {
        super.onServiceConnected()

        val config = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        }
        serviceInfo = config
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName ?: return
            var currentlyRunningApplication = launcherApplicationsHelper
                .getApplicationInfoForPackageNames(packageName.toString())
                .firstOrNull()
            if (currentlyRunningApplication?.packageName == BuildConfig.APPLICATION_ID && !startphoneApp.isInForeground) {
                currentlyRunningApplication = null
            }
            currentlyRunningApplication?.let {
                launcherApplicationsHelper.updateCurrentlyRunningApplication(it)
            }

            Log.d(TAG, "Currently running app: $currentlyRunningApplication")

//            val className = event.className ?: return
//            val componentName = ComponentName(packageName.toString(), className.toString())
//            val activityInfo = tryGetActivity(componentName)
//            activityInfo?.let {
//                Log.d(TAG, "Current activity: ${componentName.flattenToShortString()}")
//            }
        }
    }

    private fun tryGetActivity(componentName: ComponentName): ActivityInfo? {
        return try {
            packageManager.getActivityInfo(componentName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "tryGetActivity Failure: ${e.message}", e)
            null
        }
    }

    override fun onInterrupt() {
        // Do nothing
    }
}