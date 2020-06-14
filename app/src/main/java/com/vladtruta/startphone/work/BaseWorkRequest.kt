package com.vladtruta.startphone.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.vladtruta.startphone.repository.IAppRepo
import org.koin.core.KoinComponent
import org.koin.core.inject

abstract class BaseWorkRequest(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params), KoinComponent {

    companion object {
        @JvmStatic
        protected val RETRY_LIMIT = 3
    }

    protected val gson by inject<Gson>()
    protected val applicationRepository: IAppRepo by inject()
}