package com.notifii.app.util

object Constants {
    const val NOTIFICATION_CHANNEL_ID_ALARM = "notifii_alarm_channel"
    const val NOTIFICATION_CHANNEL_ID_SERVICE = "notifii_service_channel"
    const val NOTIFICATION_CHANNEL_NAME_ALARM = "Critical Alerts"
    const val NOTIFICATION_CHANNEL_NAME_SERVICE = "Background Service"

    const val ACTION_START_ALARM = "com.notifii.app.ACTION_START_ALARM"
    const val ACTION_DISMISS_ALARM = "com.notifii.app.ACTION_DISMISS_ALARM"
    const val ACTION_STOP_ALARM = "com.notifii.app.ACTION_STOP_ALARM"
    const val ACTION_SNOOZE_ALARM = "com.notifii.app.ACTION_SNOOZE_ALARM"

    const val EXTRA_RULE_ID = "extra_rule_id"
    const val EXTRA_RULE_LABEL = "extra_rule_label"
    const val EXTRA_NOTIFICATION_TITLE = "extra_notification_title"
    const val EXTRA_NOTIFICATION_TEXT = "extra_notification_text"
    const val EXTRA_APP_PACKAGE = "extra_app_package"
    const val EXTRA_ALARM_SOUND_URI = "extra_alarm_sound_uri"
    const val EXTRA_VIBRATION_PATTERN = "extra_vibration_pattern"
    const val EXTRA_USE_TTS = "extra_use_tts"

    const val PREF_KEY_MASTER_ENABLED = "pref_master_enabled"
    const val PREF_KEY_DND_OVERRIDE = "pref_dnd_override"
    const val PREF_KEY_MAX_VOLUME = "pref_max_volume"
    const val PREF_KEY_FLIP_TO_SILENCE = "pref_flip_to_silence"
    const val PREF_KEY_ONBOARDING_COMPLETED = "pref_onboarding_completed"

    const val ALARM_NOTIFICATION_ID = 1001
    const val SERVICE_NOTIFICATION_ID = 1002
}
