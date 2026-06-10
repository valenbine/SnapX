package com.example.screenshotapp.service

import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.view.WindowManager
import com.example.screenshotapp.model.QualityLevel
import com.example.screenshotapp.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.nio.ByteBuffer

class ScreenshotService(private val context: Context) {
    
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: android.hardware.display.VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null
    
    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing
    
    private val _lastScreenshot = MutableStateFlow<Bitmap?>(null)
    val lastScreenshot: StateFlow<Bitmap?> = _lastScreenshot
    
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    
    private var screenWidth = 0
    private var screenHeight = 0
    private var screenDensity = 0
    
    private var qualityLevel = QualityLevel.HIGH
    
    fun initialize(resultCode: Int, resultData: android.content.Intent) {
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData)
        
        mediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                stopCapture()
            }
        }, null)
        
        setupDisplayMetrics()
        setupImageReader()
        setupVirtualDisplay()
    }
    
    private fun setupDisplayMetrics() {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        
        screenWidth = when (qualityLevel) {
            QualityLevel.HIGH -> metrics.widthPixels
            QualityLevel.MEDIUM -> (metrics.widthPixels * 0.75).toInt()
            QualityLevel.LOW -> (metrics.widthPixels * 0.5).toInt()
        }
        
        screenHeight = when (qualityLevel) {
            QualityLevel.HIGH -> metrics.heightPixels
            QualityLevel.MEDIUM -> (metrics.heightPixels * 0.75).toInt()
            QualityLevel.LOW -> (metrics.heightPixels * 0.5).toInt()
        }
        
        screenDensity = metrics.densityDpi
    }
    
    private fun setupImageReader() {
        handlerThread = HandlerThread("ScreenshotHandlerThread")
        handlerThread?.start()
        handler = Handler(handlerThread!!.looper)
        
        imageReader = ImageReader.newInstance(
            screenWidth,
            screenHeight,
            android.graphics.PixelFormat.RGBA_8888,
            2
        )
        
        imageReader?.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            if (image != null) {
                processImage(image)
                image.close()
            }
        }, handler)
    }
    
    private fun setupVirtualDisplay() {
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenshotVirtualDisplay",
            screenWidth,
            screenHeight,
            screenDensity,
            android.hardware.display.VirtualDisplay.FLAG_AUTO,
            imageReader?.surface,
            null,
            handler
        )
    }
    
    fun setQuality(quality: QualityLevel) {
        qualityLevel = quality
        if (mediaProjection != null) {
            stopCapture()
            setupDisplayMetrics()
            setupImageReader()
            setupVirtualDisplay()
        }
    }
    
    private fun processImage(image: Image) {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * screenWidth
        
        val bitmap = Bitmap.createBitmap(
            screenWidth + rowPadding / pixelStride,
            screenHeight,
            Bitmap.Config.ARGB_8888
        )
        
        bitmap.copyPixelsFromBuffer(buffer)
        
        val croppedBitmap = if (rowPadding > 0) {
            Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight)
        } else {
            bitmap
        }
        
        _lastScreenshot.value = croppedBitmap
        _isCapturing.value = false
    }
    
    suspend fun capture(): Bitmap? {
        _isCapturing.value = true
        
        return kotlinx.coroutines.withTimeoutOrNull(Constants.RETRY_DELAY_MS * Constants.MAX_RETRY_COUNT) {
            kotlinx.coroutines.flow.first { it != null }
        }
    }
    
    suspend fun captureWithRetry(): Bitmap? {
        var retryCount = 0
        
        while (retryCount < Constants.MAX_RETRY_COUNT) {
            try {
                val bitmap = capture()
                if (bitmap != null) {
                    return bitmap
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            retryCount++
            kotlinx.coroutines.delay(Constants.RETRY_DELAY_MS)
        }
        
        return null
    }
    
    fun stopCapture() {
        virtualDisplay?.release()
        virtualDisplay = null
        
        imageReader?.close()
        imageReader = null
        
        handlerThread?.quitSafely()
        handlerThread = null
        handler = null
        
        mediaProjection?.stop()
        mediaProjection = null
        
        _isCapturing.value = false
        _lastScreenshot.value = null
    }
    
    fun isInitialized(): Boolean {
        return mediaProjection != null && virtualDisplay != null
    }
}