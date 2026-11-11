package com.notifii.app.util

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BurstDetector @Inject constructor() {

    // package -> list of timestamps
    private val notificationTimestamps = ConcurrentHashMap<String, CopyOnWriteArrayList<Long>>()
    // ruleId -> last triggered timestamp
    private val ruleTriggerTimes = ConcurrentHashMap<Long, Long>()

    fun recordNotification(packageName: String, timestamp: Long = System.currentTimeMillis()) {
        val timestamps = notificationTimestamps.getOrPut(packageName) { CopyOnWriteArrayList() }
        timestamps.add(timestamp)

        // Prune older than 1 hour to prevent memory leaks
        val cutoff = timestamp - 3_600_000L
        timestamps.removeIf { it < cutoff }
    }

    fun getRecentCount(packageName: String, windowMs: Long, now: Long = System.currentTimeMillis()): Int {
        val timestamps = notificationTimestamps[packageName] ?: return 0
        val cutoff = now - windowMs
        return timestamps.count { it >= cutoff }
    }

    fun recordTrigger(ruleId: Long, timestamp: Long = System.currentTimeMillis()) {
        ruleTriggerTimes[ruleId] = timestamp
    }

    fun getLastTriggerTime(ruleId: Long): Long {
        return ruleTriggerTimes[ruleId] ?: 0L
    }

    fun clear() {
        notificationTimestamps.clear()
        ruleTriggerTimes.clear()
    }
}
