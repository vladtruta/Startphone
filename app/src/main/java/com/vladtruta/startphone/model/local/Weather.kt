package com.vladtruta.startphone.model.local

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Weather(
    val main: String,
    val iconUrl: String
) : Parcelable