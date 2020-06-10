package com.vladtruta.startphone.util

import android.telephony.*
import androidx.annotation.RequiresPermission
import org.koin.core.KoinComponent
import org.koin.core.inject

object MobileSignalHelper : KoinComponent {

    private val telephonyManager by inject<TelephonyManager>()
    private val listeners = mutableListOf<MobileSignalListener>()

    private val phoneStateListener = object : PhoneStateListener() {
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
            super.onSignalStrengthsChanged(signalStrength)

            listeners.forEach { it.onMobileSignalStrengthChanged(signalStrength?.level ?: 0) }
        }
    }

    fun registerPhoneStateListener() {
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
    }

    fun unregisterPhoneStateListener() {
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
    }

    @RequiresPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
    fun calculateSignalStrength(): Int {
        return when (val cellInfo = telephonyManager.allCellInfo.firstOrNull()) {
            is CellInfoGsm -> {
                cellInfo.cellSignalStrength.level
            }
            is CellInfoLte -> {
                cellInfo.cellSignalStrength.level
            }
            else -> 0
        }
    }

    fun addListener(listener: MobileSignalListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: MobileSignalListener) {
        listeners.remove(listener)
    }

    fun clearListeners() {
        listeners.clear()
    }

    interface MobileSignalListener {
        fun onMobileSignalStrengthChanged(strength: Int)
    }
}