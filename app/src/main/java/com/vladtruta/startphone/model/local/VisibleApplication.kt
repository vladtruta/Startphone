package com.vladtruta.startphone.model.local

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VisibleApplication(
    val applicationInfo: ApplicationInfo,
    var isVisible: Boolean
): Parcelable