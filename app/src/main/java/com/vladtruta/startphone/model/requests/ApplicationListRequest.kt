package com.vladtruta.startphone.model.requests

import com.google.gson.annotations.SerializedName

data class ApplicationListRequest(
    @SerializedName("applications")
    val applications: List<ApplicationRequest>
) {
    data class ApplicationRequest(
        @SerializedName("packageName")
        val packageName: String,
        @SerializedName("name")
        val name: String
    )
}