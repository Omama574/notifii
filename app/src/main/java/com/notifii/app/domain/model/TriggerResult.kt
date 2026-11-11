package com.notifii.app.domain.model

data class TriggerResult(
    val shouldTrigger: Boolean,
    val matchedRule: AlertRule? = null,
    val reason: String = "",
    val notificationText: String = ""
)
