package com.notifii.app

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import com.notifii.app.billing.Billing
import com.notifii.app.data.NotifiiDb
import com.notifii.app.data.Prefs
import com.notifii.app.service.Digest

/** The whole object graph. No DI framework: it's three singletons. */
class NotifiiApp : Application() {
    val db by lazy { NotifiiDb.create(this) }
    val dao by lazy { db.dao() }
    val prefs by lazy { Prefs(this) }
    val billing by lazy { Billing(this, BuildConfig.PRO_PRODUCT) { prefs.setPro(it) } }

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannels(
            listOf(
                // The Siren service plays its own looping sound, so this channel stays silent.
                NotificationChannel(CHANNEL_SIREN, "Siren", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "The alarm screen for notifications you put on Siren."
                    setSound(null, null)
                    setBypassDnd(true)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                },
                // Used only if Android refuses to start the Siren service; the channel itself rings.
                NotificationChannel(CHANNEL_SIREN_FALLBACK, "Siren (backup)", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Rings when the full Siren can’t start."
                    setSound(
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build(),
                    )
                    setBypassDnd(true)
                    enableVibration(true)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                },
                NotificationChannel(CHANNEL_DIGEST, "Later digest", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Notifications you put on Later, delivered together."
                },
            )
        )
        Digest.schedule(this)
    }

    /** Opens the app on a tab: "now", "ladder" or "vault". */
    fun openApp(tab: String, requestCode: Int = tab.hashCode()): PendingIntent = PendingIntent.getActivity(
        this, requestCode,
        android.content.Intent(this, MainActivity::class.java).putExtra(EXTRA_TAB, tab)
            .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    companion object {
        const val CHANNEL_SIREN = "siren"
        const val CHANNEL_SIREN_FALLBACK = "siren_fallback"
        const val CHANNEL_DIGEST = "digest"
        const val EXTRA_TAB = "tab"
    }
}

val Context.notifii: NotifiiApp get() = applicationContext as NotifiiApp
