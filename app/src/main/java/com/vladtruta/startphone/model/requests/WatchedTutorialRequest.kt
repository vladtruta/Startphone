package com.vladtruta.startphone.model.requests

import com.google.gson.annotations.SerializedName

data class WatchedTutorialRequest(
    @SerializedName("tutorialId")
    val tutorialId: Int,
    @SerializedName("useful")
    val useful: Boolean
)