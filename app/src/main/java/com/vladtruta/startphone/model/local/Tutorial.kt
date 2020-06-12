package com.vladtruta.startphone.model.local

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Tutorial(
    val packageName: String,
    val title: String,
    val description: String,
    val videoUrl: String
) : Parcelable