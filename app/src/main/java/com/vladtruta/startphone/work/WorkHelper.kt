package com.vladtruta.startphone.work

import android.app.Application
import android.util.Log
import androidx.work.*
import com.vladtruta.startphone.model.local.ApplicationInfo

class WorkHelper(private val application: Application) {

    companion object {
        private const val TAG = "WorkHelper"

        const val TAG_SEND_APPLICATIONS = "TAG_SEND_APPLICATIONS"
        const val TAG_SEND_WATCHED_TUTORIALS = "TAG_SEND_WATCHED_TUTORIALS"
        const val TAG_SEND_RATED_TUTORIALS = "TAG_SEND_RATED_TUTORIALS"
        const val TAG_SEND_MISSING_TUTORIALS = "TAG_SEND_MISSING_TUTORIALS"

        private val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }

    fun enqueueApplicationsRequest(applications: List<ApplicationInfo>) {
        val data = Data.Builder()
            .putStringArray(
                ApplicationsWorkRequest.ARG_APPLICATION_PACKAGE_NAMES,
                applications.map { it.packageName }.toTypedArray()
            )
            .putStringArray(
                ApplicationsWorkRequest.ARG_APPLICATION_PACKAGE_LABELS,
                applications.map { it.label }.toTypedArray()
            )
            .build()

        val work = OneTimeWorkRequestBuilder<ApplicationsWorkRequest>()
            .setConstraints(constraints)
            .setInputData(data)
            .addTag(TAG_SEND_APPLICATIONS)
            .build()

        WorkManager.getInstance(application).enqueue(work)
        Log.d(TAG, "enqueueApplicationsRequest")
    }

    fun enqueueMissingTutorialRequest(packageName: String) {
        val work = OneTimeWorkRequestBuilder<MissingTutorialWorkRequest>()
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    MissingTutorialWorkRequest.ARG_PACKAGE_NAME to packageName
                )
            )
            .addTag(TAG_SEND_MISSING_TUTORIALS)
            .build()

        WorkManager.getInstance(application).enqueue(work)
        Log.d(TAG, "enqueueMissingTutorialRequest")
    }

    fun enqueueWatchedTutorialRequest(id: Int) {
        val work = OneTimeWorkRequestBuilder<WatchedTutorialWorkRequest>()
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    WatchedTutorialWorkRequest.ARG_TUTORIAL_ID to id
                )
            )
            .addTag(TAG_SEND_WATCHED_TUTORIALS)
            .build()

        WorkManager.getInstance(application).enqueue(work)
        Log.d(TAG, "enqueueWatchedTutorialRequest")
    }

    fun enqueueRatedTutorialRequest(id: Int, useful: Boolean) {
        val work = OneTimeWorkRequestBuilder<RatedTutorialWorkRequest>()
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    RatedTutorialWorkRequest.ARG_TUTORIAL_ID to id,
                    RatedTutorialWorkRequest.ARG_USEFUL to useful
                )
            )
            .addTag(TAG_SEND_RATED_TUTORIALS)
            .build()

        WorkManager.getInstance(application).enqueue(work)
        Log.d(TAG, "enqueueRatedTutorialRequest")
    }

    fun clearWork() {
        WorkManager.getInstance(application).cancelAllWork()
        WorkManager.getInstance(application).pruneWork()
    }
}