package com.notifii.app.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.notifii.app.R
import com.notifii.app.NotifiiApp
import com.notifii.app.notifii
import com.notifii.app.data.DIGEST_TIMES
import com.notifii.app.siren.Siren
import com.notifii.app.siren.SirenPayload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

/** Boot, digest delivery and snooze all arrive here. */
class Receivers : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_DIGEST -> {
                val pending = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try { Digest.deliver(context) } finally { Digest.schedule(context); pending.finish() }
                }
            }
            ACTION_SNOOZE -> {
                Siren.stop(context)
                val payload = SirenPayload.from(intent.extras) ?: return
                context.getSystemService(AlarmManager::class.java).setWindow(
                    AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + SNOOZE_MS, 60_000L,
                    PendingIntent.getBroadcast(
                        context, 31,
                        Intent(context, Receivers::class.java).setAction(ACTION_RESIREN).putExtras(payload.toBundle()),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                    ),
                )
            }
            ACTION_RESIREN -> SirenPayload.from(intent.extras)?.let { Siren.start(context, it) }
            else -> Digest.schedule(context) // boot / app update: alarms don't survive either
        }
    }

    companion object {
        const val ACTION_DIGEST = "com.notifii.DIGEST"
        const val ACTION_SNOOZE = "com.notifii.SNOOZE"
        const val ACTION_RESIREN = "com.notifii.RESIREN"
        private const val SNOOZE_MS = 5 * 60_000L

        fun snoozeIntent(context: Context, p: SirenPayload): PendingIntent = PendingIntent.getBroadcast(
            context, 30,
            Intent(context, Receivers::class.java).setAction(ACTION_SNOOZE).putExtras(p.toBundle()),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}

/** "Later" delivery: one calm notification at set times instead of a buzz per message. */
object Digest {
    /** Next delivery: 12:00, 18:00, and the end of Night (so held messages greet you in the morning). */
    fun nextTime(now: Long, nightOn: Boolean, nightEnd: Int): Long {
        val times = DIGEST_TIMES + if (nightOn) listOf(nightEnd) else emptyList()
        val cal = Calendar.getInstance()
        return times.map { minute ->
            cal.timeInMillis = now
            cal.set(Calendar.HOUR_OF_DAY, minute / 60); cal.set(Calendar.MINUTE, minute % 60)
            cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            if (cal.timeInMillis <= now) cal.add(Calendar.DAY_OF_YEAR, 1)
            cal.timeInMillis
        }.min()
    }

    fun schedule(context: Context) {
        val prefs = context.notifii.prefs
        val at = nextTime(System.currentTimeMillis(), prefs.nightOn.value, prefs.nightEnd.value)
        // Inexact window: no exact-alarm permission needed, and a few minutes either way is fine for a digest.
        context.getSystemService(AlarmManager::class.java).setWindow(AlarmManager.RTC_WAKEUP, at, 10 * 60_000L, intent(context))
    }

    suspend fun deliver(context: Context) {
        val app = context.notifii
        val items = app.dao.pendingOnce()
        if (items.isEmpty()) return
        val style = NotificationCompat.InboxStyle()
        items.take(6).forEach { style.addLine("${it.appLabel}: ${it.title.ifBlank { it.text }}") }
        if (items.size > 6) style.setSummaryText("+${items.size - 6} more")
        val title = if (items.size == 1) "1 notification waited for you" else "${items.size} notifications waited for you"
        val apps = items.map { it.appLabel }.distinct()
        val n = NotificationCompat.Builder(context, NotifiiApp.CHANNEL_DIGEST)
            .setSmallIcon(R.drawable.ic_stat_notifii)
            .setColor(ContextCompat.getColor(context, R.color.notifii_brand))
            .setContentTitle(title)
            .setContentText(apps.take(3).joinToString(", ") + if (apps.size > 3) " and more" else "")
            .setStyle(style)
            .setContentIntent(app.openApp("now"))
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(DIGEST_ID, n) } // no-op without notification permission
        app.dao.deliverAll()
    }

    private fun intent(context: Context) = PendingIntent.getBroadcast(
        context, 20, Intent(context, Receivers::class.java).setAction(Receivers.ACTION_DIGEST),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private const val DIGEST_ID = 4500
}
