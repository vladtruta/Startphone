package com.vladtruta.startphone.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.vladtruta.startphone.model.local.FormattedDateTime
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class DateTimeHelper {
    companion object {
        private const val TAG = "DateTimeHelper"
    }

    private val listeners = mutableListOf<DateTimeListener>()

    private val timeTickReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_TIME_TICK -> {
                    listeners.forEach { it.onDateTimeTick(getCurrentDateAndTime()) }
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

    fun getCurrentDateAndTime(): FormattedDateTime {
        val currentDateTime = DateTime.now()
        val formatter = DateTimeFormat.forPattern("HH:mm")
        val formattedTime = formatter.print(currentDateTime)

        val formattedDateTime =  FormattedDateTime(
            formattedTime,
            currentDateTime.year().asString,
            currentDateTime.monthOfYear().asText,
            currentDateTime.dayOfMonth().asString,
            currentDateTime.dayOfWeek().asText
        )

        Log.d(TAG, "getCurrentDateAndTime - formattedDateTime: $formattedDateTime")

        return formattedDateTime
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
        fun onDateTimeTick(dateTime: FormattedDateTime)
    }
}