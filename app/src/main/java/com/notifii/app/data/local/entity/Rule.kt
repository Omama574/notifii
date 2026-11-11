package com.notifii.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rules")
data class Rule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appPackage: String = "",
    val label: String = "",
    val keywords: String = "",
    val excludeKeywords: String = "",
    val regexPattern: String = "",
    val senderFilter: String = "",
    val burstCount: Int = 0,
    val burstWindowMs: Long = 0L,
    val scheduleStartHour: Int = -1,
    val scheduleStartMinute: Int = 0,
    val scheduleEndHour: Int = -1,
    val scheduleEndMinute: Int = 0,
    val scheduleDays: Int = 0x7F, // bitmask Mon=1..Sun=64, default all days
    val isEnabled: Boolean = true,
    val alarmSoundUri: String = "",
    val vibrationPattern: String = "0,500,200,500",
    val useTts: Boolean = false,
    val cooldownMs: Long = 0L,
    val priority: Int = 0,
    val triggerOnScreenOff: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
