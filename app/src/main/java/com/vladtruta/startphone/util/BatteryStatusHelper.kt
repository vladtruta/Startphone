package com.vladtruta.startphone.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import org.koin.core.KoinComponent
import org.koin.core.inject

object BatteryStatusHelper : KoinComponent {

    private const val TAG = "BatteryStatusHelper"

    private val batteryManager by inject<BatteryManager>()
    private val listeners = mutableListOf<BatteryStatusListener>()

    private val batteryStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_BATTERY_CHANGED -> {
                    val batteryStatus = getBatteryStatusForIntent(intent)
                    val percentage = batteryStatus.first
                    val isCharging = batteryStatus.second

                    listeners.forEach { it.onBatteryLevelChanged(percentage, isCharging) }
                }
            }
        }
    }

    private fun getBatteryStatusForIntent(intent: Intent): Pair<Int, Boolean> {
        val status: Int = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING

        val batteryPercentage = intent.let {
            val level: Int = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

            level / scale.toFloat()
        }

        val batteryPercentageInteger = (batteryPercentage * 100).toInt()

        Log.d(TAG, "getBatteryStatusForIntent - percentage: $batteryPercentageInteger, isCharging: $isCharging")

        return Pair(batteryPercentageInteger, isCharging)
    }

    fun getBatteryStatus(): Pair<Int, Boolean> {
        val batteryPercentage = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val isCharging = batteryManager.isCharging

        Log.d(TAG, "getBatteryStatus - percentage: $batteryPercentage, isCharging: $isCharging")

        return Pair(batteryPercentage, isCharging)
    }

    fun registerBatteryReceiver(context: Context) {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryStatusReceiver, intentFilter)
    }

    fun unregisterBatteryReceiver(context: Context) {
        context.unregisterReceiver(batteryStatusReceiver)
    }

    fun addListener(listener: BatteryStatusListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: BatteryStatusListener) {
        listeners.remove(listener)
    }

    fun clearListeners() {
        listeners.clear()
    }

    interface BatteryStatusListener {
        fun onBatteryLevelChanged(percentage: Int, isCharging: Boolean)
    }
}