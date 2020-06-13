package com.vladtruta.startphone.repository

import com.vladtruta.startphone.model.local.ApplicationInfo
import com.vladtruta.startphone.model.local.Tutorial
import org.joda.time.DateTime

interface IAppRepo {
    suspend fun getTutorialsForPackageName(packageName: String): List<Tutorial>
    suspend fun updateUser(id: String, email: String, gender: Char, dateOfBirth: DateTime)
    suspend fun updateApplications(applications: List<ApplicationInfo>)
    suspend fun updateMissingTutorial(packageName: String)
    suspend fun updateWatchedTutorial(id: Int, useful: Boolean)
}