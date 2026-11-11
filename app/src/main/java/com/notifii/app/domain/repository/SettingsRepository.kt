package com.notifii.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun getString(key: String, default: String = ""): String
    suspend fun setString(key: String, value: String)
    suspend fun getBoolean(key: String, default: Boolean = false): Boolean
    suspend fun setBoolean(key: String, value: Boolean)
    suspend fun getInt(key: String, default: Int = 0): Int
    suspend fun setInt(key: String, value: Int)
    fun observeString(key: String): Flow<String?>
    suspend fun remove(key: String)
}
