/*
 * Copyright (C) 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vladtruta.startphone.work

import android.content.Context
import androidx.work.WorkerParameters
import com.vladtruta.startphone.model.local.ApplicationInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApplicationsWorkRequest(context: Context, params: WorkerParameters) :
    BaseWorkRequest(context, params) {
    companion object {
        const val ARG_APPLICATIONS = "ARG_APPLICATIONS"
    }

    override suspend fun doWork(): Result {
        val applications = withContext(Dispatchers.Default) {
            val applicationsSerialized =
                inputData.getString(ARG_APPLICATIONS) ?: return@withContext null
            val applicationsArray =
                gson.fromJson(applicationsSerialized, Array<ApplicationInfo>::class.java)
            return@withContext applicationsArray.toList()
        } ?: return Result.failure()

        return withContext(Dispatchers.IO) {
            try {
                applicationRepository.updateApplications(applications)
                Result.success()
            } catch (error: Exception) {
                if (runAttemptCount <= RETRY_LIMIT) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        }
    }
}