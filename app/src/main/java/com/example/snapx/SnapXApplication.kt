package com.example.snapx

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.snapx.data.database.AppDatabase
import com.example.snapx.data.datastore.SettingsDataStore
import com.example.snapx.service.CleanupWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SnapXApplication : Application() {
    
    private val applicationScope = CoroutineScope(Dispatchers.Default)
    
    lateinit var database: AppDatabase
    lateinit var settingsDataStore: SettingsDataStore
    
    override fun onCreate() {
        super.onCreate()
        
        instance = this
        
        database = AppDatabase.getDatabase(this)
        settingsDataStore = SettingsDataStore(this)
        
        createNotificationChannel()
        scheduleCleanupWork()
    }
    
private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "X截图服务",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "X截图后台服务通知"
            setShowBadge(false)
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java) as NotificationManager
notificationManager.createNotificationChannel(channel)
    }
    
    private fun scheduleCleanupWork() {
        applicationScope.launch {
            val cleanDays = settingsDataStore.getAutoCleanDays()
            
            val cleanupWork = PeriodicWorkRequestBuilder<CleanupWorker>(
                cleanDays.toLong(),
                TimeUnit.DAYS
            ).build()
            
            WorkManager.getInstance(this@ScreenshotApplication).enqueueUniquePeriodicWork(
                CLEANUP_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                cleanupWork
            )
        }
    }
    
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "screenshot_service_channel"
        const val CLEANUP_WORK_NAME = "cleanup_temp_files"
        
        lateinit var instance: SnapXApplication
            private set
        
        fun getDatabase(context: Context): AppDatabase {
            return instance.database
        }
        
        fun getSettingsDataStore(context: Context): SettingsDataStore {
            return instance.settingsDataStore
        }
    }