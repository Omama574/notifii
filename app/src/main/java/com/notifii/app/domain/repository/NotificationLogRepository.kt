package com.notifii.app.domain.repository

import com.notifii.app.data.local.entity.NotificationLog
import kotlinx.coroutines.flow.Flow

interface NotificationLogRepository {
    fun getAllLogs(): Flow<List<NotificationLog>>
    fun getRecentLogs(limit: Int): Flow<List<NotificationLog>>
    fun getRecentAlerts(limit: Int): Flow<List<NotificationLog>>
    suspend fun getLogsSince(packageName: String, sinceTimestamp: Long): List<NotificationLog>
    suspend fun insertLog(log: NotificationLog): Long
    suspend fun deleteOldLogs(beforeTimestamp: Long)
    suspend fun clearAllLogs()
    fun getTotalAlertCount(): Flow<Int>
    suspend fun markAsRead(logId: Long)
}
