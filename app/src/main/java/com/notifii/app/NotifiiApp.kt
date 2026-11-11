package com.notifii.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.notifii.app.util.Constants
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NotifiiApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val alarmChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID_ALARM,
                Constants.NOTIFICATION_CHANNEL_NAME_ALARM,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channels for persistent Notifii alarms"
                enableVibration(true)
                setBypassDnd(true)
            }

            val serviceChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID_SERVICE,
                Constants.NOTIFICATION_CHANNEL_NAME_SERVICE,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for Notifii background service"
            }

            notificationManager.createNotificationChannel(alarmChannel)
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }
}
