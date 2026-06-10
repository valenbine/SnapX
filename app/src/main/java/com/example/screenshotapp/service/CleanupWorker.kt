package com.example.screenshotapp.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.screenshotapp.data.storage.FileStorageManager
import kotlinx.coroutines.flow.first

class CleanupWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        val dataStore = com.example.screenshotapp.data.datastore.SettingsDataStore(applicationContext)
        val cleanDays = dataStore.getAutoCleanDays().first()
        
        val storageManager = FileStorageManager(applicationContext)
        
        storageManager.cleanupSegments()
        storageManager.cleanupCache()
        storageManager.cleanupOldFiles(cleanDays)
        
        return Result.success()
    }
}