package com.vladtruta.startphone.model.responses

import com.google.gson.annotations.SerializedName

data class BaseResponse<out T>(
    @SerializedName("success")
    val isSuccessful: Boolean,
    @SerializedName("data")
    val data: T,
    @SerializedName("error")
    val error: String? = null
)