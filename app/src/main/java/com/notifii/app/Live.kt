package com.notifii.app

import android.app.Notification
import android.app.PendingIntent

/**
 * Things Android won't let us save to disk: a notification's tap target and its reply action.
 * They live as long as the process does; after a restart, "Open" falls back to launching the app.
 */
object Live {
    private fun <K, V> bounded(max: Int) = object : LinkedHashMap<K, V>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?) = size > max
    }

    /** Vault entry id → the original notification's tap action. */
    val taps: MutableMap<Long, PendingIntent> = java.util.Collections.synchronizedMap(bounded(300))

    /** Vault entry id → the original notification's inline-reply action. */
    val replies: MutableMap<Long, Notification.Action> = java.util.Collections.synchronizedMap(bounded(50))
}
