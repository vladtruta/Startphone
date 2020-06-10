package com.vladtruta.startphone.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import org.koin.core.KoinComponent
import org.koin.core.inject

object WifiConnectionHelper : KoinComponent {

    private const val MAX_LEVELS = 5

    private val wifiManager by inject<WifiManager>()
    private val listeners = mutableListOf<WifiConnectionListener>()

    private val wifiConnectionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                WifiManager.RSSI_CHANGED_ACTION -> {
                    val dbm = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -100)
                    val signalLevel = calculateSignalLevel(dbm)

                    listeners.forEach { it.onWifiSignalLevelChanged(signalLevel) }
                }
            }
        }
    }

    fun calculateSignalLevel(rssi: Int = wifiManager.connectionInfo.rssi): Int {
        return WifiManager.calculateSignalLevel(rssi, MAX_LEVELS)
    }

    fun registerWifiConnectionReceiver(context: Context) {
        val intentFilter = IntentFilter(WifiManager.RSSI_CHANGED_ACTION)
        context.registerReceiver(wifiConnectionReceiver, intentFilter)
    }

    fun unregisterWifiConnectionReceiver(context: Context) {
        context.unregisterReceiver(wifiConnectionReceiver)
    }

    fun addListener(listener: WifiConnectionListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: WifiConnectionListener) {
        listeners.remove(listener)
    }

    fun clearListeners() {
        listeners.clear()
    }

    interface WifiConnectionListener {
        fun onWifiSignalLevelChanged(level: Int)
    }
}