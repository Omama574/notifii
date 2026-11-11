package com.notifii.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.notifii.app.data.local.dao.AppSettingDao
import com.notifii.app.data.local.dao.NotificationLogDao
import com.notifii.app.data.local.dao.RuleDao
import com.notifii.app.data.local.entity.AppSetting
import com.notifii.app.data.local.entity.NotificationLog
import com.notifii.app.data.local.entity.Rule

@Database(
    entities = [Rule::class, NotificationLog::class, AppSetting::class],
    version = 1,
    exportSchema = false
)
abstract class NotifiiDatabase : RoomDatabase() {
    abstract fun ruleDao(): RuleDao
    abstract fun notificationLogDao(): NotificationLogDao
    abstract fun appSettingDao(): AppSettingDao
}
