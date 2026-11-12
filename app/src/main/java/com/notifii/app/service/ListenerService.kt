package com.notifii.app.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.notifii.app.Live
import com.notifii.app.data.Placement
import com.notifii.app.data.Rule
import com.notifii.app.data.Rung
import com.notifii.app.data.VaultEntry
import com.notifii.app.engine.BURST_WINDOW_MS
import com.notifii.app.engine.Note
import com.notifii.app.engine.decide
import com.notifii.app.engine.inWindow
import com.notifii.app.notifii
import com.notifii.app.siren.Siren
import com.notifii.app.siren.SirenPayload
import com.notifii.app.ui.reasonText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Calendar

/** Sees every notification, decides its rung, acts, and files it in the Vault with the reason. */
class ListenerService : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile private var placements: List<Placement> = emptyList()
    @Volatile private var rules: List<Rule> = emptyList()
    /** Last title+text per notification key, so progress updates and re-posts aren't counted twice. */
    private val lastSeen = HashMap<String, String>()
    private val arrivals = HashMap<String, ArrayDeque<Long>>()

    private var watchers: List<Job> = emptyList()

    override fun onListenerConnected() {
        val app = notifii
        watchers.forEach { it.cancel() }
        watchers = listOf(
            scope.launch { app.dao.placements().collect { placements = it } },
            scope.launch { app.dao.rules().collect { rules = it } },
        )
        scope.launch {
            val days = app.prefs.keepDays.value
            if (days > 0) app.dao.deleteOlderThan(System.currentTimeMillis() - days * 24L * 60 * 60_000)
        }
        Digest.schedule(this)
    }

    override fun onListenerDisconnected() {
        watchers.forEach { it.cancel() }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName) return
        val n = sbn.notification
        if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        // Music players, navigation, downloads: not messages, and Android won't let us hide them anyway.
        if (!sbn.isClearable || n.flags and Notification.FLAG_ONGOING_EVENT != 0 || n.flags and Notification.FLAG_FOREGROUND_SERVICE != 0) return

        val title = n.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty().trim()
        val text = (n.extras.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: n.extras.getCharSequence(Notification.EXTRA_TEXT))
            ?.toString().orEmpty().trim()
        if (title.isEmpty() && text.isEmpty()) return
        synchronized(lastSeen) {
            if (lastSeen[sbn.key] == "$title\u0000$text") return
            lastSeen[sbn.key] = "$title\u0000$text"
            if (lastSeen.size > 500) lastSeen.clear()
        }

        val sender = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(n)
            ?.messages?.lastOrNull()?.person?.name?.toString()
            ?: title.takeIf { n.category == Notification.CATEGORY_MESSAGE || n.category == Notification.CATEGORY_CALL }
        val appLabel = runCatching { packageManager.getApplicationLabel(packageManager.getApplicationInfo(sbn.packageName, 0)).toString() }
            .getOrDefault(sbn.packageName)
        val note = Note(sbn.packageName, appLabel, title, text, sender, sbn.postTime)

        val prefs = notifii.prefs
        val now = Calendar.getInstance()
        val night = prefs.nightOn.value && inWindow(now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE), prefs.nightStart.value, prefs.nightEnd.value)
        val decision = decide(note, placements, rules, recentCount(sbn.packageName, sender, sbn.postTime), night)

        if (decision.rung == Rung.LATER || decision.rung == Rung.HIDE) cancelNotification(sbn.key)

        val replyAction = n.actions?.firstOrNull { a -> a.remoteInputs?.any { it.allowFreeFormInput } == true }
        val tap = n.contentIntent
        scope.launch {
            val entry = VaultEntry(
                    pkg = note.pkg, appLabel = appLabel, title = title, text = text, sender = sender,
                    postedAt = sbn.postTime, rung = decision.rung, why = decision.why, subject = decision.subject,
                    night = decision.night, ruleId = decision.ruleId,
                    pending = decision.rung == Rung.LATER,
            )
            val id = notifii.dao.record(entry)
            tap?.let { Live.taps[id] = it }
            if (decision.rung == Rung.SIREN) {
                replyAction?.let { Live.replies[id] = it }
                val reason = reasonText(this@ListenerService, entry, rules)
                Siren.start(this@ListenerService, SirenPayload(id, note.pkg, appLabel, title, text, reason))
            }
        }
    }

    /** How many notifications this app/person sent in the last 2 minutes, including this one. */
    private fun recentCount(pkg: String, sender: String?, at: Long): Int = synchronized(arrivals) {
        val q = arrivals.getOrPut("$pkg|${sender.orEmpty()}") { ArrayDeque() }
        q.addLast(at)
        while (q.isNotEmpty() && at - q.first() > BURST_WINDOW_MS) q.removeFirst()
        q.size
    }
}
