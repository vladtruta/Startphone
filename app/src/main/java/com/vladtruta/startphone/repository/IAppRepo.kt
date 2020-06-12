package com.vladtruta.startphone.repository

import com.vladtruta.startphone.model.local.Tutorial

interface IAppRepo {
    suspend fun getTutorialsForPackageName(packageName: String): List<Tutorial>
    suspend fun sendMissingTutorialRequest()
}