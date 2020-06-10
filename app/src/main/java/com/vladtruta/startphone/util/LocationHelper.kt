package com.vladtruta.startphone.util

import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import org.koin.core.KoinComponent
import org.koin.core.inject

object LocationHelper : KoinComponent {

    private const val TAG = "LocationHelper"

    private val fusedLocationProviderClient by inject<FusedLocationProviderClient>()
    private val listeners = mutableListOf<LocationListener>()

    fun requestLastKnownLocation() {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            listeners.forEach { it.onLastLocationRetrieved(location.latitude, location.longitude) }
        }.addOnFailureListener {
            Log.e(TAG, "requestLastKnownLocation Failure: ${it.message}", it)
        }
    }

    fun addListener(listener: LocationListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: LocationListener) {
        listeners.remove(listener)
    }

    fun clearListeners() {
        listeners.clear()
    }

    interface LocationListener {
        fun onLastLocationRetrieved(latitude: Double, longitude: Double)
    }
}