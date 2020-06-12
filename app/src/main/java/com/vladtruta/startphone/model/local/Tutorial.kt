package com.vladtruta.startphone.model.local

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Tutorial(
    val id: Int,
    val packageName: String,
    val title: String,
    val videoUrl: String
) : Parcelable