package com.vladtruta.startphone.model.responses

import com.google.gson.annotations.SerializedName
import com.vladtruta.startphone.model.local.Tutorial

data class ApiTutorialResponse(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("packageName")
    val packageName: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("videoUrl")
    val videoUrl: String? = null
) {
    fun toTutorial(): Tutorial? {
        id ?: return null
        packageName ?: return null
        title ?: return null
        videoUrl ?: return null

        return Tutorial(id, packageName, title, videoUrl)
    }
}