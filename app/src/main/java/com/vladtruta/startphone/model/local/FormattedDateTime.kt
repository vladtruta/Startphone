package com.vladtruta.startphone.model.local

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FormattedDateTime(
    val time: String,
    val year: String,
    val month: String,
    val dayOfMonth: String,
    val dayOfWeek: String
): Parcelable