package com.notifii.app.data.repository

import com.notifii.app.data.local.dao.AppSettingDao
import com.notifii.app.data.local.entity.AppSetting
import com.notifii.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingDao: AppSettingDao
) : SettingsRepository {

    override suspend fun getString(key: String, default: String): String =
        settingDao.getSetting(key)?.value ?: default

    override suspend fun setString(key: String, value: String) =
        settingDao.setSetting(AppSetting(key = key, value = value))

    override suspend fun getBoolean(key: String, default: Boolean): Boolean =
        settingDao.getSetting(key)?.value?.toBooleanStrictOrNull() ?: default

    override suspend fun setBoolean(key: String, value: Boolean) =
        settingDao.setSetting(AppSetting(key = key, value = value.toString()))

    override suspend fun getInt(key: String, default: Int): Int =
        settingDao.getSetting(key)?.value?.toIntOrNull() ?: default

    override suspend fun setInt(key: String, value: Int) =
        settingDao.setSetting(AppSetting(key = key, value = value.toString()))

    override fun observeString(key: String): Flow<String?> =
        settingDao.observeSetting(key).map { it?.value }

    override suspend fun remove(key: String) =
        settingDao.deleteSetting(key)
}
