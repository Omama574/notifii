package com.notifii.app.data.repository

import com.notifii.app.data.local.dao.NotificationLogDao
import com.notifii.app.data.local.entity.NotificationLog
import com.notifii.app.domain.repository.NotificationLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationLogRepositoryImpl @Inject constructor(
    private val logDao: NotificationLogDao
) : NotificationLogRepository {

    override fun getAllLogs(): Flow<List<NotificationLog>> =
        logDao.getAllLogs()

    override fun getRecentLogs(limit: Int): Flow<List<NotificationLog>> =
        logDao.getRecentLogs(limit)

    override fun getRecentAlerts(limit: Int): Flow<List<NotificationLog>> =
        logDao.getRecentAlerts(limit)

    override suspend fun getLogsSince(packageName: String, sinceTimestamp: Long): List<NotificationLog> =
        logDao.getLogsSince(packageName, sinceTimestamp)

    override suspend fun insertLog(log: NotificationLog): Long =
        logDao.insertLog(log)

    override suspend fun deleteOldLogs(beforeTimestamp: Long) =
        logDao.deleteOldLogs(beforeTimestamp)

    override suspend fun clearAllLogs() =
        logDao.clearAllLogs()

    override fun getTotalAlertCount(): Flow<Int> =
        logDao.getTotalAlertCount()

    override suspend fun markAsRead(logId: Long) =
        logDao.markAsRead(logId)
}
