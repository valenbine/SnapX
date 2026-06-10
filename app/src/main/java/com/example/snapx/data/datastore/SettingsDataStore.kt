package com.example.snapx.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.snapx.model.PixelOption
import com.example.snapx.model.QualityLevel
import com.example.snapx.model.StitchAlgorithm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    
    companion object {
        private val DEFAULT_PIXEL = intPreferencesKey("default_pixel")
        private val DEFAULT_ALGORITHM = stringPreferencesKey("default_algorithm")
        private val QUALITY_LEVEL = stringPreferencesKey("quality_level")
        private val STORAGE_PATH = stringPreferencesKey("storage_path")
        private val FLOATING_WINDOW_ENABLED = booleanPreferencesKey("floating_window_enabled")
        private val AUTO_CLEAN_DAYS = intPreferencesKey("auto_clean_days")
        private val MAX_LONG_SCREENSHOT_HEIGHT = intPreferencesKey("max_long_screenshot_height")
        private val AUTO_STOP_THRESHOLD_KEY = stringPreferencesKey("auto_stop_threshold")
        private val RETRY_COUNT_KEY = intPreferencesKey("retry_count")
        private val DELAY_MS_KEY = intPreferencesKey("delay_ms")
    }
    
    fun getDefaultPixel(): Flow<Int> = context.settingsDataStore.data.map { preferences ->
        preferences[DEFAULT_PIXEL] ?: PixelOption.PIXEL_100.pixel
    }
    
    suspend fun setDefaultPixel(pixel: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[DEFAULT_PIXEL] = pixel
        }
    }
    
    fun getDefaultAlgorithm(): Flow<StitchAlgorithm> = context.settingsDataStore.data.map { preferences ->
        val algorithmStr = preferences[DEFAULT_ALGORITHM] ?: "TEMPLATE_MATCH"
        StitchAlgorithm.values().find { it.name == algorithmStr } ?: StitchAlgorithm.TEMPLATE_MATCH
    }
    
    suspend fun setDefaultAlgorithm(algorithm: StitchAlgorithm) {
        context.settingsDataStore.edit { preferences ->
            preferences[DEFAULT_ALGORITHM] = algorithm.name
        }
    }
    
    fun getQualityLevel(): Flow<QualityLevel> = context.settingsDataStore.data.map { preferences ->
        val qualityStr = preferences[QUALITY_LEVEL] ?: "HIGH"
        QualityLevel.values().find { it.name == qualityStr } ?: QualityLevel.HIGH
    }
    
    suspend fun setQualityLevel(level: QualityLevel) {
        context.settingsDataStore.edit { preferences ->
            preferences[QUALITY_LEVEL] = level.name
        }
    }
    
    fun getStoragePath(): Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[STORAGE_PATH] ?: "ScreenshotApp"
    }
    
    suspend fun setStoragePath(path: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[STORAGE_PATH] = path
        }
    }
    
    fun getFloatingWindowEnabled(): Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[FLOATING_WINDOW_ENABLED] ?: true
    }
    
    suspend fun setFloatingWindowEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[FLOATING_WINDOW_ENABLED] = enabled
        }
    }
    
    fun getAutoCleanDays(): Flow<Int> = context.settingsDataStore.data.map { preferences ->
        preferences[AUTO_CLEAN_DAYS] ?: 7
    }
    
    suspend fun setAutoCleanDays(days: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[AUTO_CLEAN_DAYS] = days
        }
    }
    
    fun getMaxLongScreenshotHeight(): Flow<Int> = context.settingsDataStore.data.map { preferences ->
        preferences[MAX_LONG_SCREENSHOT_HEIGHT] ?: 10000
    }
    
    suspend fun setMaxLongScreenshotHeight(height: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[MAX_LONG_SCREENSHOT_HEIGHT] = height
        }
    }
    
    fun getAutoStopThreshold(): Flow<Float> = context.settingsDataStore.data.map { preferences ->
        val thresholdStr = preferences[AUTO_STOP_THRESHOLD_KEY] ?: "0.95"
        thresholdStr.toFloatOrNull() ?: 0.95f
    }
    
    suspend fun setAutoStopThreshold(threshold: Float) {
        context.settingsDataStore.edit { preferences ->
            preferences[AUTO_STOP_THRESHOLD_KEY] = threshold.toString()
        }
    }
    
    fun getRetryCount(): Flow<Int> = context.settingsDataStore.data.map { preferences ->
        preferences[RETRY_COUNT_KEY] ?: 3
    }
    
    suspend fun setRetryCount(count: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[RETRY_COUNT_KEY] = count
        }
    }
    
    fun getDelayMs(): Flow<Long> = context.settingsDataStore.data.map { preferences ->
        preferences[DELAY_MS_KEY]?.toLong() ?: 300L
    }
    
    suspend fun setDelayMs(delayMs: Long) {
        context.settingsDataStore.edit { preferences ->
            preferences[DELAY_MS_KEY] = delayMs.toInt()
        }
    }
    
    suspend fun clearAll() {
        context.settingsDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}