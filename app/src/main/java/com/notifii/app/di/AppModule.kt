package com.notifii.app.di

import android.content.Context
import androidx.room.Room
import com.notifii.app.data.local.NotifiiDatabase
import com.notifii.app.data.local.dao.AppSettingDao
import com.notifii.app.data.local.dao.NotificationLogDao
import com.notifii.app.data.local.dao.RuleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NotifiiDatabase {
        return Room.databaseBuilder(
            context,
            NotifiiDatabase::class.java,
            "notifii_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideRuleDao(database: NotifiiDatabase): RuleDao {
        return database.ruleDao()
    }

    @Provides
    fun provideNotificationLogDao(database: NotifiiDatabase): NotificationLogDao {
        return database.notificationLogDao()
    }

    @Provides
    fun provideAppSettingDao(database: NotifiiDatabase): AppSettingDao {
        return database.appSettingDao()
    }
}
