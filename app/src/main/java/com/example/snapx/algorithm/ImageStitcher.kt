package com.example.snapx.algorithm

import android.graphics.Bitmap
import com.example.snapx.data.storage.FileStorageManager
import com.example.snapx.model.ScreenshotMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ConcurrentLinkedQueue

class ImageStitcher(
    private val fileStorageManager: FileStorageManager,
    private val mode: ScreenshotMode
) {
    
    private val overlapDetector = OverlapDetector()
    
    private val _stitchedHeight = MutableStateFlow(0)
    val stitchedHeight: StateFlow<Int> = _stitchedHeight
    
    private val _isStitching = MutableStateFlow(false)
    val isStitching: StateFlow<Boolean> = _isStitching
    
    private val segmentsQueue = ConcurrentLinkedQueue<Bitmap>()
    private var stitchedBitmap: Bitmap? = null
    private var segmentIndex = 0
    
    private val maxCacheCount = 10
    private val segmentCache = mutableListOf<Bitmap>()
    
    fun simpleStitch(prevBitmap: Bitmap, currBitmap: Bitmap): Bitmap {
        _isStitching.value = true
        
        val overlapHeight = 20
        val prevHeight = prevBitmap.height
        
        val prevBottom = Bitmap.createBitmap(prevBitmap, 0, prevHeight - overlapHeight, prevBitmap.width, overlapHeight)
        val currTop = Bitmap.createBitmap(currBitmap, 0, 0, currBitmap.width, overlapHeight)
        
        val croppedCurr = Bitmap.createBitmap(currBitmap, 0, overlapHeight, currBitmap.width, currBitmap.height - overlapHeight)
        
        val newHeight = prevBitmap.height + croppedCurr.height
        
        val resultBitmap = Bitmap.createBitmap(prevBitmap.width, newHeight, Bitmap.Config.ARGB_8888)
        
        val canvas = android.graphics.Canvas(resultBitmap)
        canvas.drawBitmap(prevBitmap, 0f, 0f, null)
        canvas.drawBitmap(croppedCurr, 0f, prevBitmap.height.toFloat(), null)
        
        _stitchedHeight.value = newHeight
        _isStitching.value = false
        
        return resultBitmap
    }
    
    fun smartStitch(prevBitmap: Bitmap, currBitmap: Bitmap, algorithm: StitchAlgorithm = StitchAlgorithm.TEMPLATE_MATCH): StitchResult {
        _isStitching.value = true
        
        try {
            val overlapHeight = when (algorithm) {
                StitchAlgorithm.TEMPLATE_MATCH -> overlapDetector.detectOverlapByTemplate(prevBitmap, currBitmap)
                StitchAlgorithm.FEATURE_DETECT -> overlapDetector.detectOverlapByFeature(prevBitmap, currBitmap)
                StitchAlgorithm.TEMPLATE_MATCH_AUTO -> overlapDetector.detectOverlapByTemplate(prevBitmap, currBitmap)
                StitchAlgorithm.FEATURE_DETECT_AUTO -> overlapDetector.detectOverlapByFeature(prevBitmap, currBitmap)
            }
            
            if (overlapHeight == -1 || overlapHeight > currBitmap.height) {
                return StitchResult.Failure(needRetry = true, errorMessage = "Overlap detection failed")
            }
            
            val croppedCurr = Bitmap.createBitmap(currBitmap, 0, overlapHeight, currBitmap.width, currBitmap.height - overlapHeight)
            
            val verificationHeight = 20
            val prevBottom = Bitmap.createBitmap(prevBitmap, 0, prevBitmap.height - verificationHeight, prevBitmap.width, verificationHeight)
            val croppedTop = Bitmap.createBitmap(croppedCurr, 0, 0, croppedCurr.width, verificationHeight)
            
            val similarity = overlapDetector.calculateSSIM(prevBottom, croppedTop)
            
            if (similarity < 0.98) {
                return StitchResult.Failure(needRetry = true, errorMessage = "Stitch quality insufficient: $similarity")
            }
            
            val newHeight = prevBitmap.height + croppedCurr.height
            
            val resultBitmap = Bitmap.createBitmap(prevBitmap.width, newHeight, Bitmap.Config.ARGB_8888)
            
            val canvas = android.graphics.Canvas(resultBitmap)
            canvas.drawBitmap(prevBitmap, 0f, 0f, null)
            canvas.drawBitmap(croppedCurr, 0f, prevBitmap.height.toFloat(), null)
            
            _stitchedHeight.value = newHeight
            _isStitching.value = false
            
            return StitchResult.Success(resultBitmap, overlapHeight)
            
        } catch (e: Exception) {
            _isStitching.value = false
            return StitchResult.Failure(needRetry = true, errorMessage = e.message ?: "Unknown error")
        }
    }
    
    fun stitchSegment(segment: Bitmap): Bitmap? {
        if (segmentCache.size >= maxCacheCount) {
            val oldest = segmentCache.removeAt(0)
            oldest.recycle()
        }
        
        segmentCache.add(segment)
        
        val file = fileStorageManager.saveScreenshotSegment(segment, segmentIndex++)
        
        segmentsQueue.offer(segment)
        
        return processQueue()
    }
    
    private fun processQueue(): Bitmap? {
        while (segmentsQueue.isNotEmpty()) {
            val segment = segmentsQueue.poll()
            
            if (stitchedBitmap == null) {
                stitchedBitmap = segment
            } else {
                val result = smartStitch(stitchedBitmap!!, segment)
                
                if (result is StitchResult.Success) {
                    stitchedBitmap?.recycle()
                    stitchedBitmap = result.bitmap
                } else if (result is StitchResult.Failure && result.needRetry) {
                    segmentsQueue.offer(segment)
                    return null
                }
            }
        }
        
        return stitchedBitmap
    }
    
    fun getFinalBitmap(): Bitmap? {
        return stitchedBitmap
    }
    
    fun reset() {
        stitchedBitmap?.recycle()
        stitchedBitmap = null
        
        segmentCache.forEach { it.recycle() }
        segmentCache.clear()
        
        segmentsQueue.clear()
        segmentIndex = 0
        
        _stitchedHeight.value = 0
        _isStitching.value = false
        
        fileStorageManager.cleanupSegments()
    }
}

sealed class StitchResult {
    data class Success(
        val bitmap: Bitmap,
        val overlapHeight: Int
    ) : StitchResult()
    
    data class Failure(
        val needRetry: Boolean,
        val errorMessage: String
    ) : StitchResult()
}

enum class StitchAlgorithm {
    TEMPLATE_MATCH,
    FEATURE_DETECT,
    TEMPLATE_MATCH_AUTO,
    FEATURE_DETECT_AUTO
}