package com.vladtruta.startphone.util

import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient

class LocationHelper(private val fusedLocationProviderClient: FusedLocationProviderClient) {
    companion object {
        private const val TAG = "LocationHelper"
    }

    private val listeners = mutableListOf<LocationListener>()

    fun requestLastKnownLocation() {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                Log.d(
                    TAG,
                    "requestLastKnownLocation Success - latitude: ${location.latitude}, longitude: ${location.longitude}"
                )

                listeners.forEach {
                    it.onLastLocationRetrieved(
                        location.latitude,
                        location.longitude
                    )
                }
            } else {
                Log.e(TAG, "requestLastKnownLocation Failure: location is null")
            }
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