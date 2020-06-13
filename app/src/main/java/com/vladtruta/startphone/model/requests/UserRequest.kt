package com.vladtruta.startphone.model.requests

import com.google.gson.annotations.SerializedName

data class UserRequest(
    @SerializedName("id")
    val id: String,
    @SerializedName("dateOfBirth")
    val dateOfBirth: String,
    @SerializedName("gender")
    val gender: Char,
    @SerializedName("email")
    val email: String
)