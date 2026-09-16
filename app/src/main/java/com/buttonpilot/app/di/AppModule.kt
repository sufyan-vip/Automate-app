package com.buttonpilot.app.di

import android.content.Context
import androidx.room.Room
import com.buttonpilot.app.core.common.SystemTimeProvider
import com.buttonpilot.app.core.common.TimeProvider
import com.buttonpilot.app.core.logging.AndroidLogger
import com.buttonpilot.app.core.logging.Logger
import com.buttonpilot.app.data.local.AppDatabase
import com.buttonpilot.app.core.common.Constants
import dagger.Binds
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DB_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideRecordingDao(db: AppDatabase) = db.recordingDao()

    @Provides
    @Singleton
    fun provideShortcutRuleDao(db: AppDatabase) = db.shortcutRuleDao()

    @Provides
    @Singleton
    fun provideActionHistoryDao(db: AppDatabase) = db.actionHistoryDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class LoggerModule {
    @Binds
    @Singleton
    abstract fun bindLogger(impl: AndroidLogger): Logger

    @Binds
    @Singleton
    abstract fun bindTimeProvider(impl: SystemTimeProvider): TimeProvider
}
