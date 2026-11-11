package com.notifii.app.domain.model

data class CapturedNotification(
    val packageName: String,
    val appLabel: String = "",
    val title: String = "",
    val text: String = "",
    val sender: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isScreenOff: Boolean = false
)
