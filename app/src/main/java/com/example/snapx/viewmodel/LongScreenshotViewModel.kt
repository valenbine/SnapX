package com.example.snapx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapx.SnapXApplication
import com.example.snapx.model.LongScreenshotConfig
import com.example.snapx.model.PixelOption
import com.example.snapx.model.ScreenshotMode
import com.example.snapx.model.StitchAlgorithm
import com.example.snapx.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LongScreenshotViewModel(application: Application) : AndroidViewModel(application) {
    
    private val settingsDataStore = SnapXApplication.getSettingsDataStore(application)
    
    private val _selectedMode = MutableStateFlow(ScreenshotMode.LONG_PIXEL_SCROLL)
    val selectedMode: StateFlow<ScreenshotMode> = _selectedMode.asStateFlow()
    
    private val _pixelValue = MutableStateFlow(PixelOption.PIXEL_100.pixel)
    val pixelValue: StateFlow<Int> = _pixelValue.asStateFlow()
    
    private val _customPixelValue = MutableStateFlow<Int?>(null)
    val customPixelValue: StateFlow<Int?> = _customPixelValue.asStateFlow()
    
    private val _selectedAlgorithm = MutableStateFlow(StitchAlgorithm.TEMPLATE_MATCH)
    val selectedAlgorithm: StateFlow<StitchAlgorithm> = _selectedAlgorithm.asStateFlow()
    
    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()
    
    private val _capturedHeight = MutableStateFlow(0)
    val capturedHeight: StateFlow<Int> = _capturedHeight.asStateFlow()
    
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadDefaultSettings()
    }
    
    private fun loadDefaultSettings() {
        viewModelScope.launch {
            settingsDataStore.getDefaultPixel().collect { pixel ->
                _pixelValue.value = pixel
            }
        }
        
        viewModelScope.launch {
            settingsDataStore.getDefaultAlgorithm().collect { algorithm ->
                _selectedAlgorithm.value = algorithm
            }
        }
    }
    
    fun selectMode(mode: ScreenshotMode) {
        _selectedMode.value = mode
    }
    
    fun selectPixelOption(option: PixelOption) {
        if (option == PixelOption.PIXEL_CUSTOM) {
            _customPixelValue.value = 0
        } else {
            _pixelValue.value = option.pixel
            _customPixelValue.value = null
        }
    }
    
    fun setCustomPixelValue(value: Int) {
        if (value > 0) {
            _customPixelValue.value = value
            _pixelValue.value = value
        }
    }
    
    fun selectAlgorithm(algorithm: StitchAlgorithm) {
        _selectedAlgorithm.value = algorithm
    }
    
    fun startLongScreenshot() {
        _isCapturing.value = true
        _capturedHeight.value = 0
        _progress.value = 0f
        
        viewModelScope.launch {
            try {
                val config = LongScreenshotConfig(
                    mode = _selectedMode.value,
                    pixelValue = _pixelValue.value,
                    algorithm = _selectedAlgorithm.value
                )
                
                val maxHeight = settingsDataStore.getMaxLongScreenshotHeight().first()
                _progress.value = _capturedHeight.value.toFloat() / maxHeight
                
            } catch (e: Exception) {
                _error.value = e.message
                _isCapturing.value = false
            }
        }
    }
    
    fun pauseLongScreenshot() {
        _isCapturing.value = false
    }
    
    fun resumeLongScreenshot() {
        _isCapturing.value = true
    }
    
    fun stopLongScreenshot() {
        _isCapturing.value = false
        _capturedHeight.value = 0
        _progress.value = 0f
    }
    
    fun clearError() {
        _error.value = null
    }
    
    private suspend fun <T> kotlinx.coroutines.flow.Flow<T>.first(): T {
        return kotlinx.coroutines.flow.first()
    }
}