package com.vladtruta.startphone.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object DateTimeHelper {

    private val listeners = mutableListOf<DateTimeListener>()

    private val timeTickReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_TIME_TICK -> {
                    listeners.forEach {
                        it.onDateTimeTick(
                            getCurrentTime(),
                            getCurrentYear(),
                            getCurrentMonth(),
                            getCurrentDayOfMonth(),
                            getCurrentDayOfWeek()
                        )
                    }
                }
            }
        }
    }

    fun registerTimeTickReceiver(context: Context) {
        val intentFilter = IntentFilter(Intent.ACTION_TIME_TICK)
        context.registerReceiver(timeTickReceiver, intentFilter)
    }

    fun unregisterTimeTickReceiver(context: Context) {
        context.unregisterReceiver(timeTickReceiver)
    }

    fun getCurrentTime(): String {
        val currentDateTime = DateTime.now()
        val formatter = DateTimeFormat.forPattern("HH:mm")
        return formatter.print(currentDateTime)
    }

    fun getCurrentYear(): String {
        return DateTime.now().year().asString
    }

    fun getCurrentMonth(): String {
        return DateTime.now().monthOfYear().asText
    }

    fun getCurrentDayOfMonth(): String {
        return DateTime.now().dayOfMonth().asString
    }

    fun getCurrentDayOfWeek(): String {
        return DateTime.now().dayOfWeek().asText
    }

    fun addListener(listener: DateTimeListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: DateTimeListener) {
        listeners.remove(listener)
    }

    fun clearListeners() {
        listeners.clear()
    }

    interface DateTimeListener {
        fun onDateTimeTick(
            time: String,
            year: String,
            month: String,
            dayOfMonth: String,
            dayOfWeek: String
        )
    }
}