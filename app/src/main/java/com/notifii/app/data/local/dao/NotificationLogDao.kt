package com.notifii.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.notifii.app.data.local.entity.NotificationLog
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationLogDao {

    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<NotificationLog>>

    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int): Flow<List<NotificationLog>>

    @Query("SELECT * FROM notification_logs WHERE didTriggerAlarm = 1 ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentAlerts(limit: Int): Flow<List<NotificationLog>>

    @Query("SELECT * FROM notification_logs WHERE packageName = :packageName ORDER BY timestamp DESC")
    fun getLogsByPackage(packageName: String): Flow<List<NotificationLog>>

    @Query("SELECT * FROM notification_logs WHERE packageName = :packageName AND timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    suspend fun getLogsSince(packageName: String, sinceTimestamp: Long): List<NotificationLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: NotificationLog): Long

    @Query("DELETE FROM notification_logs WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldLogs(beforeTimestamp: Long)

    @Query("DELETE FROM notification_logs")
    suspend fun clearAllLogs()

    @Query("SELECT COUNT(*) FROM notification_logs WHERE didTriggerAlarm = 1")
    fun getTotalAlertCount(): Flow<Int>

    @Query("UPDATE notification_logs SET wasRead = 1 WHERE id = :logId")
    suspend fun markAsRead(logId: Long)
}
