package com.vladtruta.startphone.util

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat

class NotificationsHelper(private val notificationManager: NotificationManager) {
    @TargetApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(
        channelId: String,
        name: String,
        description: String,
        importance: Int,
        groupId: String? = null,
        showBadge: Boolean = true,
        forceDisableSound: Boolean = false,
        forceDisableVibration: Boolean = false
    ) {
        val channel = NotificationChannel(channelId, name, importance).apply {
            this.description = description
            this.lightColor = Color.RED
            this.setShowBadge(showBadge)

            if (forceDisableSound) {
                this.setSound(null, null)
            }

            if (forceDisableVibration) {
                this.enableVibration(false)
            }
        }

        // Set the notification channel groupId (if there is one)
        groupId?.let { channel.group = groupId }

        // Register the channel with the system
        notificationManager.createNotificationChannel(channel)
    }

    fun createNotification(
        context: Context,
        channelId: String,
        title: String,
        description: String,
        @DrawableRes iconResource: Int,
        pendingIntent: PendingIntent
    ): Notification {
        return NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(iconResource)
            .setContentIntent(pendingIntent)
            .build()
    }
}