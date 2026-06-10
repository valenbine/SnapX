package com.example.snapx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapx.SnapXApplication
import com.example.snapx.data.datastore.SettingsDataStore
import com.example.snapx.model.PixelOption
import com.example.snapx.model.QualityLevel
import com.example.snapx.model.StitchAlgorithm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val settingsDataStore = SnapXApplication.getSettingsDataStore(application)
    
    private val _defaultPixel = MutableStateFlow(PixelOption.PIXEL_100.pixel)
    val defaultPixel: StateFlow<Int> = _defaultPixel.asStateFlow()
    
    private val _defaultAlgorithm = MutableStateFlow(StitchAlgorithm.TEMPLATE_MATCH)
    val defaultAlgorithm: StateFlow<StitchAlgorithm> = _defaultAlgorithm.asStateFlow()
    
    private val _qualityLevel = MutableStateFlow(QualityLevel.HIGH)
    val qualityLevel: StateFlow<QualityLevel> = _qualityLevel.asStateFlow()
    
    private val _floatingWindowEnabled = MutableStateFlow(true)
    val floatingWindowEnabled: StateFlow<Boolean> = _floatingWindowEnabled.asStateFlow()
    
    private val _autoCleanDays = MutableStateFlow(7)
    val autoCleanDays: StateFlow<Int> = _autoCleanDays.asStateFlow()
    
    private val _storagePath = MutableStateFlow("SnapX")
    val storagePath: StateFlow<String> = _storagePath.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            settingsDataStore.getDefaultPixel().collect { pixel ->
                _defaultPixel.value = pixel
            }
        }
        
        viewModelScope.launch {
            settingsDataStore.getDefaultAlgorithm().collect { algorithm ->
                _defaultAlgorithm.value = algorithm
            }
        }
        
        viewModelScope.launch {
            settingsDataStore.getQualityLevel().collect { level ->
                _qualityLevel.value = level
            }
        }
        
        viewModelScope.launch {
            settingsDataStore.getFloatingWindowEnabled().collect { enabled ->
                _floatingWindowEnabled.value = enabled
            }
        }
        
        viewModelScope.launch {
            settingsDataStore.getAutoCleanDays().collect { days ->
                _autoCleanDays.value = days
            }
        }
        
        viewModelScope.launch {
            settingsDataStore.getStoragePath().collect { path ->
                _storagePath.value = path
            }
        }
    }
    
    fun setDefaultPixel(pixel: Int) {
        viewModelScope.launch {
            settingsDataStore.setDefaultPixel(pixel)
            _defaultPixel.value = pixel
        }
    }
    
    fun setDefaultAlgorithm(algorithm: StitchAlgorithm) {
        viewModelScope.launch {
            settingsDataStore.setDefaultAlgorithm(algorithm)
            _defaultAlgorithm.value = algorithm
        }
    }
    
    fun setQualityLevel(level: QualityLevel) {
        viewModelScope.launch {
            settingsDataStore.setQualityLevel(level)
            _qualityLevel.value = level
        }
    }
    
    fun setFloatingWindowEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setFloatingWindowEnabled(enabled)
            _floatingWindowEnabled.value = enabled
        }
    }
    
    fun setAutoCleanDays(days: Int) {
        viewModelScope.launch {
            settingsDataStore.setAutoCleanDays(days)
            _autoCleanDays.value = days
        }
    }
    
    fun setStoragePath(path: String) {
        viewModelScope.launch {
            settingsDataStore.setStoragePath(path)
            _storagePath.value = path
        }
    }
}