package com.example.screenshotapp.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import com.example.screenshotapp.model.ScreenshotMode
import com.example.screenshotapp.model.ScreenshotRecord
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileStorageManager(private val context: Context) {
    
    companion object {
        const val NORMAL_DIR = "normal"
        const val LONG_DIR = "long"
        const val TEMP_DIR = "temp"
        const val SEGMENTS_DIR = "segments"
        const val CACHE_DIR = "cache"
        
        fun getBaseStorageDir(context: Context): File {
            val externalDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            return File(externalDir, "ScreenshotApp")
        }
    }
    
    fun initializeDirectories() {
        val baseDir = getBaseStorageDir(context)
        val normalDir = File(baseDir, NORMAL_DIR)
        val longDir = File(baseDir, LONG_DIR)
        val tempDir = File(baseDir, TEMP_DIR)
        val segmentsDir = File(tempDir, SEGMENTS_DIR)
        val cacheDir = File(tempDir, CACHE_DIR)
        
        normalDir.mkdirs()
        longDir.mkdirs()
        segmentsDir.mkdirs()
        cacheDir.mkdirs()
    }
    
    fun saveScreenshot(bitmap: Bitmap, mode: ScreenshotMode): ScreenshotRecord? {
        val fileName = ScreenshotRecord.generateFileName(mode)
        val targetDir = getTargetDir(mode)
        val file = File(targetDir, fileName)
        
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            
            return ScreenshotRecord(
                fileName = fileName,
                filePath = file.absolutePath,
                mode = mode,
                timestamp = System.currentTimeMillis(),
                resolution = ScreenshotRecord.formatResolution(bitmap.width, bitmap.height),
                fileSize = file.length(),
                width = bitmap.width,
                height = bitmap.height
            )
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
    
    fun saveScreenshotSegment(bitmap: Bitmap, index: Int): File {
        val segmentsDir = File(getBaseStorageDir(context), "$TEMP_DIR/$SEGMENTS_DIR")
        val fileName = "segment_${String.format("%03d", index)}.png"
        val file = File(segmentsDir, fileName)
        
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        
        return file
    }
    
    fun getTargetDir(mode: ScreenshotMode): File {
        val baseDir = getBaseStorageDir(context)
        return when (mode) {
            ScreenshotMode.NORMAL -> File(baseDir, NORMAL_DIR)
            ScreenshotMode.LONG_SCREEN_BY_SCREEN,
            ScreenshotMode.LONG_PIXEL_SCROLL,
            ScreenshotMode.LONG_AUTO_DETECT -> File(baseDir, LONG_DIR)
        }
    }
    
    fun deleteScreenshot(record: ScreenshotRecord): Boolean {
        val file = File(record.filePath)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }
    
    fun cleanupSegments() {
        val segmentsDir = File(getBaseStorageDir(context), "$TEMP_DIR/$SEGMENTS_DIR")
        if (segmentsDir.exists() && segmentsDir.isDirectory) {
            segmentsDir.listFiles()?.forEach { file ->
                file.delete()
            }
        }
    }
    
    fun cleanupCache() {
        val cacheDir = File(getBaseStorageDir(context), "$TEMP_DIR/$CACHE_DIR")
        if (cacheDir.exists() && cacheDir.isDirectory) {
            cacheDir.listFiles()?.forEach { file ->
                file.delete()
            }
        }
    }
    
    fun cleanupOldFiles(days: Int) {
        val thresholdTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        
        cleanupOldFilesInDir(File(getBaseStorageDir(context), NORMAL_DIR), thresholdTime)
        cleanupOldFilesInDir(File(getBaseStorageDir(context), LONG_DIR), thresholdTime)
    }
    
    private fun cleanupOldFilesInDir(dir: File, thresholdTime: Long) {
        if (dir.exists() && dir.isDirectory) {
            dir.listFiles()?.forEach { file ->
                if (file.lastModified() < thresholdTime) {
                    file.delete()
                }
            }
        }
    }
    
    fun getAvailableStorageSpace(): Long {
        val stat = android.os.StatFs(getBaseStorageDir(context).absolutePath)
        return stat.availableBlocksLong * stat.blockSizeLong
    }
    
    fun isStorageAvailable(requiredSize: Long): Boolean {
        return getAvailableStorageSpace() > requiredSize
    }
}