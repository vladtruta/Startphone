package com.vladtruta.startphone.util

import android.annotation.SuppressLint
import android.telephony.*
import android.util.Log
import org.koin.core.KoinComponent
import org.koin.core.inject

object MobileSignalHelper : KoinComponent {

    private const val TAG = "MobileSignalHelper"

    private val telephonyManager by inject<TelephonyManager>()
    private val listeners = mutableListOf<MobileSignalListener>()

    private val phoneStateListener = object : PhoneStateListener() {
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
            super.onSignalStrengthsChanged(signalStrength)

            val level = signalStrength?.level ?: 0

            Log.d(TAG, "onSignalStrengthsChanged - level: $level")

            listeners.forEach { it.onMobileSignalStrengthChanged(signalStrength?.level ?: 0) }
        }
    }

    fun registerPhoneStateListener() {
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
    }

    fun unregisterPhoneStateListener() {
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
    }

    fun calculateSignalStrength(): Int {
        val signalStrength = when (val cellInfo = telephonyManager.allCellInfo.firstOrNull()) {
            is CellInfoGsm -> {
                cellInfo.cellSignalStrength.level
            }
            is CellInfoLte -> {
                cellInfo.cellSignalStrength.level
            }
            else -> 0
        }

        Log.d(TAG, "calculateSignalStrength - signalStrength: $signalStrength")

        return signalStrength
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