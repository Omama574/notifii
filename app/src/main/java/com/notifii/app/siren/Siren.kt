package com.notifii.app.siren

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.notifii.app.NotifiiApp
import com.notifii.app.R
import kotlinx.coroutines.flow.MutableStateFlow

/** What's ringing. Travels in Intent extras between the listener, service, screen and snooze alarm. */
data class SirenPayload(
    val entryId: Long,
    val pkg: String,
    val appLabel: String,
    val title: String,
    val text: String,
    val reason: String,
) {
    fun toBundle() = Bundle().apply {
        putLong("entryId", entryId); putString("pkg", pkg); putString("appLabel", appLabel)
        putString("title", title); putString("text", text); putString("reason", reason)
    }

    companion object {
        fun from(b: Bundle?): SirenPayload? {
            b ?: return null
            val pkg = b.getString("pkg") ?: return null
            return SirenPayload(
                b.getLong("entryId"), pkg, b.getString("appLabel").orEmpty(),
                b.getString("title").orEmpty(), b.getString("text").orEmpty(), b.getString("reason").orEmpty(),
            )
        }
    }
}

object SirenState {
    val active = MutableStateFlow<SirenPayload?>(null)
    val startedAt = MutableStateFlow(0L)
    val muted = MutableStateFlow(false)
}

object Siren {
    const val ACTION_START = "com.notifii.siren.START"
    const val ACTION_MUTE = "com.notifii.siren.MUTE"
    const val ACTION_STOP = "com.notifii.siren.STOP"
    private const val FALLBACK_ID = 4401

    fun start(context: Context, payload: SirenPayload) {
        val intent = Intent(context, SirenService::class.java).setAction(ACTION_START).putExtras(payload.toBundle())
        try {
            ContextCompat.startForegroundService(context, intent)
        } catch (e: IllegalStateException) {
            // Android 12+ can refuse background service starts. The backup channel rings on its own until opened.
            fallback(context, payload)
        }
    }

    fun mute(context: Context) = send(context, ACTION_MUTE)
    fun stop(context: Context) = send(context, ACTION_STOP)

    private fun send(context: Context, action: String) {
        if (SirenState.active.value == null) return
        runCatching { context.startService(Intent(context, SirenService::class.java).setAction(action)) }
    }

    fun screenIntent(context: Context, payload: SirenPayload): PendingIntent = PendingIntent.getActivity(
        context, 7,
        Intent(context, SirenActivity::class.java).putExtras(payload.toBundle()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private fun fallback(context: Context, payload: SirenPayload) {
        val n = NotificationCompat.Builder(context, NotifiiApp.CHANNEL_SIREN_FALLBACK)
            .setSmallIcon(R.drawable.ic_stat_notifii)
            .setColor(ContextCompat.getColor(context, R.color.notifii_siren))
            .setContentTitle(payload.title.ifBlank { payload.appLabel })
            .setContentText(payload.text)
            .setSubText("Siren · ${payload.appLabel}")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(screenIntent(context, payload), true)
            .setContentIntent(screenIntent(context, payload))
            .setAutoCancel(true)
            .build()
        n.flags = n.flags or Notification.FLAG_INSISTENT // repeat the sound until it's opened
        context.getSystemService(NotificationManager::class.java).notify(FALLBACK_ID, n)
    }

    fun clearFallback(context: Context) = context.getSystemService(NotificationManager::class.java).cancel(FALLBACK_ID)
}
