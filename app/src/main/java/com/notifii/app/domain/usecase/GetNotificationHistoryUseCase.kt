package com.notifii.app.domain.usecase

import com.notifii.app.data.local.entity.NotificationLog
import com.notifii.app.domain.repository.NotificationLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationHistoryUseCase @Inject constructor(
    private val logRepository: NotificationLogRepository
) {

    fun getAllLogs(): Flow<List<NotificationLog>> =
        logRepository.getAllLogs()

    fun getRecentLogs(limit: Int = 50): Flow<List<NotificationLog>> =
        logRepository.getRecentLogs(limit)

    fun getRecentAlerts(limit: Int = 20): Flow<List<NotificationLog>> =
        logRepository.getRecentAlerts(limit)

    fun getTotalAlertCount(): Flow<Int> =
        logRepository.getTotalAlertCount()

    suspend fun clearHistory() =
        logRepository.clearAllLogs()

    suspend fun markAsRead(logId: Long) =
        logRepository.markAsRead(logId)

    suspend fun deleteOldLogs(daysOld: Int = 30) {
        val cutoff = System.currentTimeMillis() - (daysOld.toLong() * 24 * 60 * 60 * 1000)
        logRepository.deleteOldLogs(cutoff)
    }
}
