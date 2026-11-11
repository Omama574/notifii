package com.notifii.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_logs")
data class NotificationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appLabel: String = "",
    val title: String = "",
    val text: String = "",
    val sender: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val matchedRuleId: Long? = null,
    val matchedRuleLabel: String = "",
    val didTriggerAlarm: Boolean = false,
    val wasRead: Boolean = false
)
