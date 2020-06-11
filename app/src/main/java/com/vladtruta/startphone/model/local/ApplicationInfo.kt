package com.vladtruta.startphone.model.local

import android.graphics.drawable.Drawable
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class ApplicationInfo(
    val label: String,
    val packageName: String,
    val icon: @RawValue Drawable
) : Parcelable