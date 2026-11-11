package com.notifii.app.di

import com.notifii.app.data.repository.NotificationLogRepositoryImpl
import com.notifii.app.data.repository.RuleRepositoryImpl
import com.notifii.app.data.repository.SettingsRepositoryImpl
import com.notifii.app.domain.repository.NotificationLogRepository
import com.notifii.app.domain.repository.RuleRepository
import com.notifii.app.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRuleRepository(
        impl: RuleRepositoryImpl
    ): RuleRepository

    @Binds
    @Singleton
    abstract fun bindNotificationLogRepository(
        impl: NotificationLogRepositoryImpl
    ): NotificationLogRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository
}
