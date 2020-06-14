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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MissingTutorialWorkRequest(context: Context, params: WorkerParameters) :
    BaseWorkRequest(context, params) {
    companion object {
        const val ARG_PACKAGE_NAME = "ARG_PACKAGE_NAME"
    }

    override suspend fun doWork(): Result {
        val packageName = inputData.getString(ARG_PACKAGE_NAME) ?: return Result.failure()

        return withContext(Dispatchers.IO) {
            try {
                applicationRepository.updateMissingTutorial(packageName)
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