package com.notifii.app.domain.usecase

import com.notifii.app.domain.model.AlertRule
import com.notifii.app.domain.model.CapturedNotification
import com.notifii.app.domain.model.TriggerResult
import com.notifii.app.domain.repository.NotificationLogRepository
import com.notifii.app.domain.repository.RuleRepository
import com.notifii.app.util.BurstDetector
import com.notifii.app.util.RegexMatcher
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EvaluateNotificationUseCase @Inject constructor(
    private val ruleRepository: RuleRepository,
    private val logRepository: NotificationLogRepository,
    private val burstDetector: BurstDetector,
    private val regexMatcher: RegexMatcher
) {

    suspend operator fun invoke(notification: CapturedNotification): TriggerResult {
        val enabledRules = ruleRepository.getEnabledRulesSnapshot()
        val combinedText = "${notification.title} ${notification.text}".lowercase()

        for (rule in enabledRules.sortedByDescending { it.priority }) {
            if (matchesRule(rule, notification, combinedText)) {
                return TriggerResult(
                    shouldTrigger = true,
                    matchedRule = rule,
                    reason = "Matched rule: ${rule.label}",
                    notificationText = notification.text
                )
            }
        }

        return TriggerResult(shouldTrigger = false, reason = "No rule matched")
    }

    private suspend fun matchesRule(
        rule: AlertRule,
        notification: CapturedNotification,
        combinedText: String
    ): Boolean {
        // 1. Check screen-off condition
        if (!rule.triggerOnScreenOff && notification.isScreenOff) {
            return false
        }

        // 2. App package filter
        if (rule.appPackage.isNotBlank() && rule.appPackage != notification.packageName) {
            return false
        }

        // 3. Sender filter
        if (rule.senderFilter.isNotBlank()) {
            val senderMatch = notification.sender.lowercase().contains(rule.senderFilter.lowercase()) ||
                notification.title.lowercase().contains(rule.senderFilter.lowercase())
            if (!senderMatch) return false
        }

        // 4. Keyword inclusion
        if (rule.keywords.isNotEmpty()) {
            val hasKeyword = rule.keywords.any { keyword ->
                combinedText.contains(keyword.lowercase().trim())
            }
            if (!hasKeyword) return false
        }

        // 5. Keyword exclusion
        if (rule.excludeKeywords.isNotEmpty()) {
            val hasExcluded = rule.excludeKeywords.any { keyword ->
                combinedText.contains(keyword.lowercase().trim())
            }
            if (hasExcluded) return false
        }

        // 6. Regex match
        if (rule.regexPattern.isNotBlank()) {
            if (!regexMatcher.matches(combinedText, rule.regexPattern)) {
                return false
            }
        }

        // 7. Schedule check
        if (!isWithinSchedule(rule)) {
            return false
        }

        // 8. Burst detection
        if (rule.burstCount > 0 && rule.burstWindowMs > 0) {
            val recentCount = burstDetector.getRecentCount(
                notification.packageName,
                rule.burstWindowMs
            )
            if (recentCount < rule.burstCount) {
                return false
            }
        }

        // 9. Cooldown check
        if (rule.cooldownMs > 0) {
            val lastTrigger = burstDetector.getLastTriggerTime(rule.id)
            if (lastTrigger > 0 && (notification.timestamp - lastTrigger) < rule.cooldownMs) {
                return false
            }
        }

        // Record trigger time for cooldown
        burstDetector.recordTrigger(rule.id, notification.timestamp)

        return true
    }

    private fun isWithinSchedule(rule: AlertRule): Boolean {
        if (rule.scheduleStartHour < 0 || rule.scheduleEndHour < 0) {
            return true // No schedule set
        }

        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
        // Convert Calendar day (Sun=1..Sat=7) to our bitmask (Mon=bit0..Sun=bit6)
        val dayBit = when (currentDay) {
            Calendar.MONDAY -> 0x01
            Calendar.TUESDAY -> 0x02
            Calendar.WEDNESDAY -> 0x04
            Calendar.THURSDAY -> 0x08
            Calendar.FRIDAY -> 0x10
            Calendar.SATURDAY -> 0x20
            Calendar.SUNDAY -> 0x40
            else -> 0x00
        }

        if (rule.scheduleDays and dayBit == 0) {
            return false
        }

        val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val startMinutes = rule.scheduleStartHour * 60 + rule.scheduleStartMinute
        val endMinutes = rule.scheduleEndHour * 60 + rule.scheduleEndMinute

        return if (startMinutes <= endMinutes) {
            currentMinutes in startMinutes..endMinutes
        } else {
            // Overnight schedule (e.g., 22:00 - 06:00)
            currentMinutes >= startMinutes || currentMinutes <= endMinutes
        }
    }
}
