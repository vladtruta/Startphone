package com.vladtruta.startphone.model.requests

import com.google.gson.annotations.SerializedName

data class MissingTutorialRequest(
    @SerializedName("packageName")
    val packageName: String
)