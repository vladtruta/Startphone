package com.vladtruta.startphone.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent

@SuppressLint("LongLogTag")
class WindowChangeDetectService : AccessibilityService() {
    companion object {
        private const val TAG = "WindowChangeDetectService"

        var currentlyRunningApplicationLabel = ""
            private set
    }

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
            currentlyRunningApplicationLabel = getApplicationNameFromPackageName(packageName.toString())

            Log.d(TAG, "Current app label: $currentlyRunningApplicationLabel")

            val className = event.className ?: return
            val componentName = ComponentName(packageName.toString(), className.toString())
            val activityInfo = tryGetActivity(componentName)
            activityInfo?.let {
                Log.d(TAG, "Current activity: ${componentName.flattenToShortString()}")
            }
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

    private fun getApplicationNameFromPackageName(packageName: String): String {
        val applicationInfo = try {
            packageManager.getApplicationInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }

        return (applicationInfo?.let { packageManager.getApplicationLabel(it) }
            ?: "Application").toString()
    }

    override fun onInterrupt() {
        // Do nothing
    }
}