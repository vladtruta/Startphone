package com.vladtruta.startphone.repository

import com.vladtruta.startphone.model.local.Tutorial
import com.vladtruta.startphone.webservice.IAppApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApplicationRepository(private val applicationApi: IAppApi) : IAppRepo {

    override suspend fun getTutorialsForPackageName(packageName: String): List<Tutorial> {
        return withContext(Dispatchers.IO) {
            try {
                val tutorialResponse = applicationApi.getTutorialsForPackageName(packageName)
                if (!tutorialResponse.isSuccessful) {
                    throw Exception("API returned unsuccessful result")
                }

                return@withContext tutorialResponse.data
                    .map {
                        it.toTutorial()
                            ?: throw Exception("Invalid tutorial with id: ${it.id} for package name: $packageName")
                    }
            } catch (error: Exception) {
                throw Exception("Failed to get tutorials", error)
            }
        }
    }

}