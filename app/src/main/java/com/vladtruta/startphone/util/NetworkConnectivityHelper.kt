package com.vladtruta.startphone.util

import android.annotation.SuppressLint
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log

@SuppressLint("LongLogTag")
class NetworkConnectivityHelper(private val connectivityManager: ConnectivityManager) {
    companion object {
        private const val TAG = "NetworkConnectivityHelper"
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val listeners = mutableListOf<NetworkConnectivityListener>()

    private var networkConnected = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            mainHandler.post {
                networkConnected = true
                listeners.forEach { it.onNetworkConnected() }
            }
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            mainHandler.post {
                val hasWifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                listeners.forEach { it.onNetworkStateChanged(hasWifi) }
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            mainHandler.post {
                networkConnected = false
                listeners.forEach { it.onNetworkDisconnected() }
            }
        }
    }

    fun registerNetworkCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            connectivityManager.registerNetworkCallback(
                NetworkRequest.Builder().build(),
                networkCallback
            )
        }
    }

    fun unregisterNetworkCallback() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "unregisterNetworkCallback Failure: ${e.message}", e)
        }
    }

    @Suppress("DEPRECATION")
    fun isNetworkConnected(): Boolean {
        return connectivityManager.activeNetworkInfo?.isConnected ?: false
    }

    fun isWifiConnected(): Boolean {
        return networkConnected && connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
    }

    fun isMobileDataConnected(): Boolean {
        return networkConnected && connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            ?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false
    }

    fun addListener(listener: NetworkConnectivityListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: NetworkConnectivityListener) {
        listeners.remove(listener)
    }

    fun clearListeners() {
        listeners.clear()
    }

    interface NetworkConnectivityListener {
        fun onNetworkConnected()
        fun onNetworkStateChanged(isWifi: Boolean)
        fun onNetworkDisconnected()
    }
}