package com.notifii.app.domain.model

data class AlertRule(
    val id: Long = 0,
    val appPackage: String = "",
    val label: String = "",
    val keywords: List<String> = emptyList(),
    val excludeKeywords: List<String> = emptyList(),
    val regexPattern: String = "",
    val senderFilter: String = "",
    val burstCount: Int = 0,
    val burstWindowMs: Long = 0L,
    val scheduleStartHour: Int = -1,
    val scheduleStartMinute: Int = 0,
    val scheduleEndHour: Int = -1,
    val scheduleEndMinute: Int = 0,
    val scheduleDays: Int = 0x7F,
    val isEnabled: Boolean = true,
    val alarmSoundUri: String = "",
    val vibrationPattern: String = "0,500,200,500",
    val useTts: Boolean = false,
    val cooldownMs: Long = 0L,
    val priority: Int = 0,
    val triggerOnScreenOff: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
