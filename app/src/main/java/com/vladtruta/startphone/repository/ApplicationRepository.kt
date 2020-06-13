package com.vladtruta.startphone.repository

import com.vladtruta.startphone.model.local.ApplicationInfo
import com.vladtruta.startphone.model.local.Tutorial
import com.vladtruta.startphone.model.requests.ApplicationListRequest
import com.vladtruta.startphone.model.requests.MissingTutorialRequest
import com.vladtruta.startphone.model.requests.UserRequest
import com.vladtruta.startphone.model.requests.WatchedTutorialRequest
import com.vladtruta.startphone.util.PreferencesHelper
import com.vladtruta.startphone.webservice.IAppApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.DateTime


class ApplicationRepository(private val applicationApi: IAppApi, private val preferencesHelper: PreferencesHelper) : IAppRepo {

    override suspend fun getTutorialsForPackageName(packageName: String): List<Tutorial> {
        return withContext(Dispatchers.IO) {
            try {
                val response = applicationApi.getTutorialsForPackageName(packageName)
                if (!response.isSuccessful) {
                    throw Exception("API returned unsuccessful result")
                }

                return@withContext response.data
                    .map {
                        it.toTutorial()
                            ?: throw Exception("Invalid tutorial with id: ${it.id} for package name: $packageName")
                    }
            } catch (e: Exception) {
                throw Exception("Error when trying to get tutorials", e)
            }
        }
    }

    override suspend fun updateUser(
        id: String,
        email: String,
        gender: Char,
        dateOfBirth: DateTime
    ) {
        withContext(Dispatchers.IO) {
            val dayOfBirthEpoch = dateOfBirth.toInstant()
            val userRequest = UserRequest(id, dayOfBirthEpoch.toString(), gender, email)
            try {
                val response = applicationApi.updateUser(userRequest)
                if (!response.isSuccessful) {
                    throw Exception(response.error)
                } else {
                    preferencesHelper.saveAuthorizationToken(response.data)
                }
            } catch (e: Exception) {
                throw Exception("Error when trying to sign up", e)
            }
        }
    }

    override suspend fun updateApplications(applications: List<ApplicationInfo>) {
        withContext(Dispatchers.IO) {
            val applicationList = applications.map {
                ApplicationListRequest.ApplicationRequest(
                    it.packageName,
                    it.label
                )
            }
            val request = ApplicationListRequest(applicationList)
            try {
                val response = applicationApi.updateApplications(request)
                if (!response.isSuccessful) {
                    throw Exception(response.error)
                }
            } catch (e: Exception) {
                throw Exception(
                    "Error when trying to send applications, applications: $applications",
                    e
                )
            }
        }
    }

    override suspend fun updateMissingTutorial(packageName: String) {
        withContext(Dispatchers.IO) {
            val request = MissingTutorialRequest(packageName)
            try {
                val response = applicationApi.updateMissingTutorial(request)
                if (!response.isSuccessful) {
                    throw Exception(response.error)
                }
            } catch (e: Exception) {
                throw Exception(
                    "Error when trying to send missing tutorial, packageName: $packageName",
                    e
                )
            }
        }
    }

    override suspend fun updateWatchedTutorial(id: Int, useful: Boolean) {
        withContext(Dispatchers.IO) {
            val request = WatchedTutorialRequest(id, useful)
            try {
                val response = applicationApi.updateWatchedTutorial(request)
                if (!response.isSuccessful) {
                    throw Exception(response.error)
                }
            } catch (e: Exception) {
                throw Exception(
                    "Error when trying to update watched tutorial, id: $id, useful: $useful",
                    e
                )
            }
        }
    }
}