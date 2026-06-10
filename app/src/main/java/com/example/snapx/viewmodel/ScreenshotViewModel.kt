package com.example.snapx.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapx.SnapXApplication
import com.example.snapx.data.database.ScreenshotEntity
import com.example.snapx.data.storage.FileStorageManager
import com.example.snapx.model.LongScreenshotConfig
import com.example.snapx.model.PixelOption
import com.example.snapx.model.QualityLevel
import com.example.snapx.model.ScreenshotMode
import com.example.snapx.model.StitchAlgorithm
import com.example.snapx.service.ScreenshotForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScreenshotViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _hasMediaProjectionPermission = MutableStateFlow(false)
    val hasMediaProjectionPermission: StateFlow<Boolean> = _hasMediaProjectionPermission.asStateFlow()
    
    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()
    
    private val _lastScreenshot = MutableStateFlow<Bitmap?>(null)
    val lastScreenshot: StateFlow<Bitmap?> = _lastScreenshot.asStateFlow()
    
    private val _lastScreenshotId = MutableStateFlow<Long?>(null)
    val lastScreenshotId: StateFlow<Long?> = _lastScreenshotId.asStateFlow()
    
    private val _selectedDelay = MutableStateFlow(0)
    val selectedDelay: StateFlow<Int> = _selectedDelay.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private var resultCode: Int = 0
    private var resultData: Intent? = null
    
    fun handleMediaProjectionResult(code: Int, data: Intent) {
        resultCode = code
        resultData = data
        _hasMediaProjectionPermission.value = true
        
        ScreenshotForegroundService.startService(getApplication(), resultCode, resultData!!)
    }
    
    fun captureFullScreen(delay: Int) {
        _selectedDelay.value = delay
        _isCapturing.value = true
        
        viewModelScope.launch {
            try {
                if (delay > 0) {
                    kotlinx.coroutines.delay(delay * 1000L)
                }
                
                ScreenshotForegroundService.captureNormal(getApplication())
                
                kotlinx.coroutines.delay(500)
                
                val database = SnapXApplication.getDatabase(getApplication())
                val latestRecords = database.screenshotDao().getAll()
                latestRecords.collect { records ->
                    if (records.isNotEmpty()) {
                        _lastScreenshotId.value = records.first().id
                        _isCapturing.value = false
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isCapturing.value = false
            }
        }
    }
    
    fun captureArea(delay: Int) {
        _selectedDelay.value = delay
        _isCapturing.value = true
        
        viewModelScope.launch {
            try {
                if (delay > 0) {
                    kotlinx.coroutines.delay(delay * 1000L)
                }
                
                ScreenshotForegroundService.captureNormal(getApplication())
                
                kotlinx.coroutines.delay(500)
                
                val database = SnapXApplication.getDatabase(getApplication())
                val latestRecords = database.screenshotDao().getAll()
                latestRecords.collect { records ->
                    if (records.isNotEmpty()) {
                        _lastScreenshotId.value = records.first().id
                        _isCapturing.value = false
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isCapturing.value = false
            }
        }
    }
    
    fun shareScreenshot() {
        val screenshotId = _lastScreenshotId.value
        if (screenshotId == null) {
            return
        }
        
        viewModelScope.launch {
            val database = SnapXApplication.getDatabase(getApplication())
            val record = database.screenshotDao().getById(screenshotId)
            
            if (record != null) {
                val file = java.io.File(record.filePath)
                if (file.exists()) {
                    val uri = androidx.core.content.FileProvider.getUriForFile(
                        getApplication(),
                        "${getApplication().packageName}.fileprovider",
                        file
                    )
                    
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "image/png"
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    
                    val chooserIntent = Intent.createChooser(shareIntent, "分享截图")
                    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    getApplication().startActivity(chooserIntent)
                }
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}