package com.notifii.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.notifii.app.service.AlarmService
import com.notifii.app.util.Constants

class AlarmDismissReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        val stopIntent = Intent(context, AlarmService::class.java).apply {
            action = Constants.ACTION_STOP_ALARM
        }
        context.startService(stopIntent)
    }
}
