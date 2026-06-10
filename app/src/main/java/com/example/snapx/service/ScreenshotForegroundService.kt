package com.example.snapx.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.snapx.MainActivity
import com.example.snapx.R
import com.example.snapx.SnapXApplication
import com.example.snapx.model.ScreenshotMode
import com.example.snapx.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScreenshotForegroundService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var screenshotService: ScreenshotService? = null
    
    companion object {
        const val ACTION_START_CAPTURE = "com.example.snapx.START_CAPTURE"
        const val ACTION_STOP_CAPTURE = "com.example.snapx.STOP_CAPTURE"
        const val ACTION_CAPTURE_NORMAL = "com.example.snapx.CAPTURE_NORMAL"
        const val ACTION_CAPTURE_LONG = "com.example.snapx.CAPTURE_LONG"
        
        const val EXTRA_RESULT_CODE = "resultCode"
        const val EXTRA_RESULT_DATA = "resultData"
        const val EXTRA_MODE = "mode"
        
        private var isRunning = false
        
        fun isServiceRunning(): Boolean = isRunning
        
        fun startService(context: Context, resultCode: Int, resultData: Intent) {
            val intent = Intent(context, ScreenshotForegroundService::class.java)
            intent.action = ACTION_START_CAPTURE
            intent.putExtra(EXTRA_RESULT_CODE, resultCode)
            intent.putExtra(EXTRA_RESULT_DATA, resultData)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, ScreenshotForegroundService::class.java)
            intent.action = ACTION_STOP_CAPTURE
            context.startService(intent)
        }
        
        fun captureNormal(context: Context) {
            val intent = Intent(context, ScreenshotForegroundService::class.java)
            intent.action = ACTION_CAPTURE_NORMAL
            context.startService(intent)
        }
        
        fun captureLong(context: Context, mode: ScreenshotMode) {
            val intent = Intent(context, ScreenshotForegroundService::class.java)
            intent.action = ACTION_CAPTURE_LONG
            intent.putExtra(EXTRA_MODE, mode.name)
            context.startService(intent)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        isRunning = true
        screenshotService = ScreenshotService(this)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_CAPTURE -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
                val resultData = intent.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)
                
                if (resultCode != 0 && resultData != null) {
                    startForegroundWithNotification()
                    screenshotService?.initialize(resultCode, resultData)
                }
            }
            
            ACTION_STOP_CAPTURE -> {
                screenshotService?.stopCapture()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                isRunning = false
            }
            
            ACTION_CAPTURE_NORMAL -> {
                serviceScope.launch {
                    captureNormalScreenshot()
                }
            }
            
            ACTION_CAPTURE_LONG -> {
                val modeName = intent.getStringExtra(EXTRA_MODE)
                val mode = ScreenshotMode.values().find { it.name == modeName } ?: ScreenshotMode.NORMAL
                serviceScope.launch {
                    captureLongScreenshot(mode)
                }
            }
        }
        
        return START_NOT_STICKY
    }
    
    private fun startForegroundWithNotification() {
        val notification = createNotification()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                Constants.NOTIFICATION_ID_SCREENSHOT_SERVICE,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(Constants.NOTIFICATION_ID_SCREENSHOT_SERVICE, notification)
        }
    }
    
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, SnapXApplication.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.capturing))
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    private suspend fun captureNormalScreenshot() {
        val bitmap = screenshotService?.captureWithRetry()
        if (bitmap != null) {
            saveScreenshot(bitmap, ScreenshotMode.NORMAL)
        }
    }
    
    private suspend fun captureLongScreenshot(mode: ScreenshotMode) {
        // 长图截图逻辑将在第二阶段实现
    }
    
    private fun saveScreenshot(bitmap: Bitmap, mode: ScreenshotMode) {
        serviceScope.launch {
            val storageManager = com.example.snapx.data.storage.FileStorageManager(this@ScreenshotForegroundService)
            val record = storageManager.saveScreenshot(bitmap, mode)
            
            if (record != null) {
                val entity = com.example.snapx.data.database.ScreenshotEntity(
                    fileName = record.fileName,
                    filePath = record.filePath,
                    mode = com.example.snapx.data.database.ScreenshotEntity.fromScreenshotMode(record.mode),
                    timestamp = record.timestamp,
                    pixelValue = record.pixelValue,
                    algorithm = record.algorithm?.let { 
                        com.example.snapx.data.database.ScreenshotEntity.fromStitchAlgorithm(it) 
                    },
                    resolution = record.resolution,
                    fileSize = record.fileSize,
                    width = record.width,
                    height = record.height
                )
                
                SnapXApplication.getDatabase(this@ScreenshotForegroundService)
                    .screenshotDao()
                    .insert(entity)
            }
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        screenshotService?.stopCapture()
        screenshotService = null
        isRunning = false
    }
}