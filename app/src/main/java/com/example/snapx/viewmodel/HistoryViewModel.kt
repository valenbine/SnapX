package com.example.snapx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapx.SnapXApplication
import com.example.snapx.data.database.ScreenshotEntity
import com.example.snapx.model.ScreenshotMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = SnapXApplication.getDatabase(application)
    
    private val _screenshots = MutableStateFlow<List<ScreenshotEntity>>(emptyList())
    val screenshots: StateFlow<List<ScreenshotEntity>> = _screenshots.asStateFlow()
    
    private val _selectedScreenshot = MutableStateFlow<ScreenshotEntity?>(null)
    val selectedScreenshot: StateFlow<ScreenshotEntity?> = _selectedScreenshot.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadScreenshots()
    }
    
    fun loadScreenshots() {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                database.screenshotDao().getAll().collect { records ->
                    _screenshots.value = records
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }
    
    fun loadScreenshotsByMode(mode: ScreenshotMode) {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val modeStr = ScreenshotEntity.fromScreenshotMode(mode)
                database.screenshotDao().getByMode(modeStr).collect { records ->
                    _screenshots.value = records
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }
    
    fun selectScreenshot(screenshot: ScreenshotEntity) {
        _selectedScreenshot.value = screenshot
    }
    
    fun deleteScreenshot(screenshot: ScreenshotEntity) {
        viewModelScope.launch {
            try {
                val file = java.io.File(screenshot.filePath)
                if (file.exists()) {
                    file.delete()
                }
                
                database.screenshotDao().delete(screenshot)
                
                if (_selectedScreenshot.value?.id == screenshot.id) {
                    _selectedScreenshot.value = null
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}