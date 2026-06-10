package com.example.screenshotapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.screenshotapp.model.ScreenshotMode
import com.example.screenshotapp.model.StitchAlgorithm

@Entity(tableName = "screenshots")
data class ScreenshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val filePath: String,
    val mode: String,
    val timestamp: Long,
    val pixelValue: Int?,
    val algorithm: String?,
    val resolution: String,
    val fileSize: Long,
    val width: Int,
    val height: Int
) {
    companion object {
        fun fromScreenshotMode(mode: ScreenshotMode): String {
            return when (mode) {
                ScreenshotMode.NORMAL -> "NORMAL"
                ScreenshotMode.LONG_SCREEN_BY_SCREEN -> "LONG_SCREEN_BY_SCREEN"
                ScreenshotMode.LONG_PIXEL_SCROLL -> "LONG_PIXEL_SCROLL"
                ScreenshotMode.LONG_AUTO_DETECT -> "LONG_AUTO_DETECT"
            }
        }
        
        fun fromStitchAlgorithm(algorithm: StitchAlgorithm): String {
            return when (algorithm) {
                StitchAlgorithm.TEMPLATE_MATCH -> "TEMPLATE_MATCH"
                StitchAlgorithm.FEATURE_DETECT -> "FEATURE_DETECT"
                StitchAlgorithm.TEMPLATE_MATCH_AUTO -> "TEMPLATE_MATCH_AUTO"
                StitchAlgorithm.FEATURE_DETECT_AUTO -> "FEATURE_DETECT_AUTO"
            }
        }
        
        fun toScreenshotMode(mode: String): ScreenshotMode {
            return when (mode) {
                "NORMAL" -> ScreenshotMode.NORMAL
                "LONG_SCREEN_BY_SCREEN" -> ScreenshotMode.LONG_SCREEN_BY_SCREEN
                "LONG_PIXEL_SCROLL" -> ScreenshotMode.LONG_PIXEL_SCROLL
                "LONG_AUTO_DETECT" -> ScreenshotMode.LONG_AUTO_DETECT
                else -> ScreenshotMode.NORMAL
            }
        }
        
        fun toStitchAlgorithm(algorithm: String): StitchAlgorithm {
            return when (algorithm) {
                "TEMPLATE_MATCH" -> StitchAlgorithm.TEMPLATE_MATCH
                "FEATURE_DETECT" -> StitchAlgorithm.FEATURE_DETECT
                "TEMPLATE_MATCH_AUTO" -> StitchAlgorithm.TEMPLATE_MATCH_AUTO
                "FEATURE_DETECT_AUTO" -> StitchAlgorithm.FEATURE_DETECT_AUTO"
                else -> StitchAlgorithm.TEMPLATE_MATCH
            }
        }
    }
}