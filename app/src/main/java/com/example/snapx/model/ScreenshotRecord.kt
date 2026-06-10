package com.example.snapx.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ScreenshotRecord(
    val id: Long = 0,
    val fileName: String,
    val filePath: String,
    val mode: ScreenshotMode,
    val timestamp: Long,
    val pixelValue: Int? = null,
    val algorithm: StitchAlgorithm? = null,
    val resolution: String,
    val fileSize: Long,
    val width: Int,
    val height: Int
) {
    companion object {
        fun generateFileName(mode: ScreenshotMode): String {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(Date())
            val modeName = when (mode) {
                ScreenshotMode.NORMAL -> "normal"
                ScreenshotMode.LONG_SCREEN_BY_SCREEN -> "long_screen"
                ScreenshotMode.LONG_PIXEL_SCROLL -> "long_pixel"
                ScreenshotMode.LONG_AUTO_DETECT -> "long_auto"
            }
            return "${timestamp}_${modeName}.png"
        }
        
        fun formatResolution(width: Int, height: Int): String {
            return "${width}x${height}"
        }
    }
    
    fun getFileSizeFormatted(): String {
        return when {
            fileSize < 1024 -> "$fileSize B"
            fileSize < 1024 * 1024 -> "${fileSize / 1024} KB"
            else -> "${fileSize / (1024 * 1024)} MB"
        }
    }
    
    fun getTimestampFormatted(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}