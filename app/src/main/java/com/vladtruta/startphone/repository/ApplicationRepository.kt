package com.vladtruta.startphone.repository

import com.vladtruta.startphone.model.local.ApplicationInfo
import com.vladtruta.startphone.model.local.Tutorial
import com.vladtruta.startphone.model.requests.*
import com.vladtruta.startphone.util.PreferencesHelper
import com.vladtruta.startphone.webservice.IAppApi
import com.vladtruta.startphone.work.WorkHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.LocalDate


class ApplicationRepository(
    private val applicationApi: IAppApi,
    private val preferencesHelper: PreferencesHelper,
    private val workHelper: WorkHelper
) : IAppRepo {

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
        dateOfBirth: LocalDate
    ) {
        withContext(Dispatchers.IO) {
            val userRequest = UserRequest(id, dateOfBirth.toString(), gender, email)
            try {
                val response = applicationApi.updateUser(userRequest)
                if (!response.isSuccessful) {
                    throw Exception(response.error)
                } else {
                    preferencesHelper.saveAuthorizationToken(response.data)
                    preferencesHelper.saveUserEmail(email)
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
                workHelper.enqueueApplicationsRequest(applications)
                throw Exception("Error when trying to send applications", e)
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
                workHelper.enqueueMissingTutorialRequest(packageName)
                throw Exception("Error when trying to send missing tutorial", e)
            }
        }
    }

    override suspend fun updateWatchedTutorial(id: Int) {
        withContext(Dispatchers.IO) {
            val request = WatchedTutorialRequest(id)
            try {
                val response = applicationApi.updateWatchedTutorial(request)
                if (!response.isSuccessful) {
                    throw Exception(response.error)
                }
            } catch (e: Exception) {
                workHelper.enqueueWatchedTutorialRequest(id)
                throw Exception("Error when trying to update watched tutorial, id: $id", e)
            }
        }
    }

    override suspend fun updateRatedTutorial(id: Int, useful: Boolean) {
        withContext(Dispatchers.IO) {
            val request = RatedTutorialRequest(id, useful)
            try {
                val response = applicationApi.updateRatedTutorial(request)
                if (!response.isSuccessful) {
                    throw Exception(response.error)
                }
            } catch (e: Exception) {
                workHelper.enqueueRatedTutorialRequest(id, useful)
                throw Exception("Error when trying to update rated tutorial", e)
            }
        }
    }
}