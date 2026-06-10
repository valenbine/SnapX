package com.example.snapx.service

import android.content.Context
import android.graphics.Bitmap
import com.example.snapx.algorithm.ImageStitcher
import com.example.snapx.algorithm.ScrollCompensator
import com.example.snapx.model.StitchAlgorithm
import com.example.snapx.algorithm.StitchResult
import com.example.snapx.data.storage.FileStorageManager
import com.example.snapx.model.LongScreenshotConfig
import com.example.snapx.model.ScreenshotMode
import com.example.snapx.util.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LongScreenshotProcessor(
    private val context: Context,
    private val screenshotService: ScreenshotService,
    private val accessibilityService: ScrollAccessibilityService?
) {
    
    private val fileStorageManager = FileStorageManager(context)
    
    private lateinit var imageStitcher: ImageStitcher
    private val scrollCompensator = ScrollCompensator()
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing
    
    private val _capturedHeight = MutableStateFlow(0)
    val capturedHeight: StateFlow<Int> = _capturedHeight
    
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress
    
    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage
    
    private val processingScope = CoroutineScope(Dispatchers.Default)
    
    private var config: LongScreenshotConfig? = null
    private var processingJob: Job? = null
    
    private var retryCount = 0
    private val maxRetry = Constants.MAX_RETRY_COUNT
    
    fun initialize(config: LongScreenshotConfig) {
        this.config = config
        
        imageStitcher = ImageStitcher(fileStorageManager, config.mode)
        scrollCompensator.reset()
        
        fileStorageManager.initializeDirectories()
    }
    
    fun startProcessing() {
        if (_isProcessing.value) {
            return
        }
        
        _isProcessing.value = true
        _capturedHeight.value = 0
        _progress.value = 0f
        _statusMessage.value = "开始截取长图..."
        
        processingJob = processingScope.launch {
            try {
                when (config?.mode) {
                    ScreenshotMode.NORMAL -> {
                        _statusMessage.value = "单屏截图不支持长图模式"
                        _isProcessing.value = false
                    }
                    ScreenshotMode.LONG_SCREEN_BY_SCREEN -> processScreenByScreen()
                    ScreenshotMode.LONG_PIXEL_SCROLL -> processPixelScroll()
                    ScreenshotMode.LONG_AUTO_DETECT -> processAutoDetect()
                    null -> {
                        _statusMessage.value = "配置错误"
                        _isProcessing.value = false
                    }
                }
                
                saveFinalResult()
                
            } catch (e: CancellationException) {
                _statusMessage.value = "已停止"
            } catch (e: Exception) {
                _statusMessage.value = "错误: ${e.message}"
                _isProcessing.value = false
            }
        }
    }
    
    private suspend fun processScreenByScreen() {
        val maxHeight = config?.maxHeight ?: Constants.MAX_LONG_SCREENSHOT_HEIGHT_DEFAULT
        
        while (_capturedHeight.value < maxHeight && _isProcessing.value) {
            accessibilityService?.scrollFullScreen()
            
            delay(config?.delayMs ?: Constants.DEFAULT_SCROLL_DELAY_MS)
            
            val bitmap = screenshotService.captureWithRetry()
            
            if (bitmap != null) {
                val prevBitmap = imageStitcher.getFinalBitmap()
                
                val result = if (prevBitmap != null) {
                    imageStitcher.simpleStitch(prevBitmap, bitmap)
                } else {
                    bitmap
                }
                
                _capturedHeight.value = result.height
                _progress.value = _capturedHeight.value.toFloat() / maxHeight
                _statusMessage.value = "已截取 ${_capturedHeight.value} px"
                
                if (accessibilityService?.isAtBottom() == true) {
                    _statusMessage.value = "已到达底部"
                    break
                }
            } else {
                retryCount++
                if (retryCount >= maxRetry) {
                    _statusMessage.value = "截图失败"
                    break
                }
                delay(Constants.RETRY_DELAY_MS)
            }
        }
    }
    
    private suspend fun processPixelScroll() {
        val maxHeight = config?.maxHeight ?: Constants.MAX_LONG_SCREENSHOT_HEIGHT_DEFAULT
        val basePixelValue = config?.pixelValue ?: 100
        
        var currentPixelValue = basePixelValue
        
        while (_capturedHeight.value < maxHeight && _isProcessing.value) {
            accessibilityService?.scrollByPixels(currentPixelValue)
            
            delay(100)
            
            val bitmap = screenshotService.captureWithRetry()
            
            if (bitmap != null) {
                val prevBitmap = imageStitcher.getFinalBitmap()
                
                if (prevBitmap != null) {
                    val result = imageStitcher.smartStitch(
                        prevBitmap,
                        bitmap,
                        config?.algorithm ?: StitchAlgorithm.TEMPLATE_MATCH
                    )
                    
                    when (result) {
                        is StitchResult.Success -> {
                            val actualOffset = result.overlapHeight
                            currentPixelValue = scrollCompensator.calculateNextScroll(basePixelValue, actualOffset)
                            
                            retryCount = 0
                            
                            _capturedHeight.value = result.bitmap.height
                            _progress.value = _capturedHeight.value.toFloat() / maxHeight
                            _statusMessage.value = "已截取 ${_capturedHeight.value} px"
                        }
                        is StitchResult.Failure -> {
                            if (result.needRetry && retryCount < maxRetry) {
                                retryCount++
                                delay(Constants.RETRY_DELAY_MS)
                                continue
                            } else {
                                _statusMessage.value = result.errorMessage
                                break
                            }
                        }
                    }
                } else {
                    imageStitcher.stitchSegment(bitmap)
                    _capturedHeight.value = bitmap.height
                    _progress.value = _capturedHeight.value.toFloat() / maxHeight
                    _statusMessage.value = "已截取 ${_capturedHeight.value} px"
                    
                    currentPixelValue = scrollCompensator.calculateNextScroll(basePixelValue, config!!.pixelValue)
                }
                
                if (accessibilityService?.isAtBottom() == true) {
                    _statusMessage.value = "已到达底部"
                    break
                }
            } else {
                retryCount++
                if (retryCount >= maxRetry) {
                    _statusMessage.value = "截图失败"
                    break
                }
                delay(Constants.RETRY_DELAY_MS)
            }
        }
    }
    
    private suspend fun processAutoDetect() {
        val maxHeight = config?.maxHeight ?: Constants.MAX_LONG_SCREENSHOT_HEIGHT_DEFAULT
        val basePixelValue = 100
        
        var previousHeight = 0
        var unchangedCount = 0
        
        while (_capturedHeight.value < maxHeight && _isProcessing.value) {
            accessibilityService?.scrollByPixels(basePixelValue)
            
            delay(config?.delayMs ?: Constants.DEFAULT_SCROLL_DELAY_MS)
            
            val bitmap = screenshotService.captureWithRetry()
            
            if (bitmap != null) {
                val prevBitmap = imageStitcher.getFinalBitmap()
                
                if (prevBitmap != null) {
                    val result = imageStitcher.smartStitch(
                        prevBitmap,
                        bitmap,
                        config?.algorithm ?: StitchAlgorithm.TEMPLATE_MATCH_AUTO
                    )
                    
                    when (result) {
                        is StitchResult.Success -> {
                            if (result.overlapHeight > bitmap.height * 0.9) {
                                val similarityThreshold = config?.autoStopThreshold ?: Constants.DEFAULT_SIMILARITY_THRESHOLD
                                val prevBottom = Bitmap.createBitmap(
                                    prevBitmap,
                                    0,
                                    prevBitmap.height - 50,
                                    prevBitmap.width,
                                    50
                                )
                                val currTop = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, 50)
                                
                                val similarity = com.example.snapx.algorithm.OverlapDetector()
                                    .calculateSSIM(prevBottom, currTop)
                                
                                if (similarity > similarityThreshold) {
                                    _statusMessage.value = "检测到底部，自动停止"
                                    break
                                }
                            }
                            
                            if (_capturedHeight.value == previousHeight) {
                                unchangedCount++
                                if (unchangedCount >= 3) {
                                    _statusMessage.value = "内容高度未变化，自动停止"
                                    break
                                }
                            } else {
                                unchangedCount = 0
                            }
                            
                            previousHeight = _capturedHeight.value
                            
                            retryCount = 0
                            
                            _capturedHeight.value = result.bitmap.height
                            _progress.value = _capturedHeight.value.toFloat() / maxHeight
                            _statusMessage.value = "已截取 ${_capturedHeight.value} px"
                        }
                        is StitchResult.Failure -> {
                            if (result.needRetry && retryCount < maxRetry) {
                                retryCount++
                                delay(Constants.RETRY_DELAY_MS)
                                continue
                            } else {
                                _statusMessage.value = result.errorMessage
                                break
                            }
                        }
                    }
                } else {
                    imageStitcher.stitchSegment(bitmap)
                    _capturedHeight.value = bitmap.height
                    _progress.value = _capturedHeight.value.toFloat() / maxHeight
                    _statusMessage.value = "已截取 ${_capturedHeight.value} px"
                }
            } else {
                retryCount++
                if (retryCount >= maxRetry) {
                    _statusMessage.value = "截图失败"
                    break
                }
                delay(Constants.RETRY_DELAY_MS)
            }
        }
    }
    
    private suspend fun saveFinalResult() {
        val finalBitmap = imageStitcher.getFinalBitmap()
        
        if (finalBitmap != null) {
            val mode = config?.mode ?: ScreenshotMode.LONG_PIXEL_SCROLL
            val record = fileStorageManager.saveScreenshot(finalBitmap, mode)
            
            if (record != null) {
                val database = com.example.snapx.SnapXApplication.getDatabase(context)
                val entity = com.example.snapx.data.database.ScreenshotEntity(
                    fileName = record.fileName,
                    filePath = record.filePath,
                    mode = com.example.snapx.data.database.ScreenshotEntity.fromScreenshotMode(record.mode),
                    timestamp = record.timestamp,
                    pixelValue = record.pixelValue,
                    algorithm = record.algorithm?.let {
                        com.example.snapx.data.database.ScreenshotEntity.fromStitchAlgorithm(it)
                    },
                    resolution = record.resolution,
                    fileSize = record.fileSize,
                    width = record.width,
                    height = record.height
                )
                
                database.screenshotDao().insert(entity)
                
                _statusMessage.value = "保存成功"
            } else {
                _statusMessage.value = "保存失败"
            }
        } else {
            _statusMessage.value = "无截图数据"
        }
        
        _isProcessing.value = false
    }
    
    fun pauseProcessing() {
        _isProcessing.value = false
        _statusMessage.value = "已暂停"
    }
    
    fun resumeProcessing() {
        if (!_isProcessing.value) {
            startProcessing()
        }
    }
    
    fun stopProcessing() {
        processingJob?.cancel()
        
        processingScope.launch {
            saveFinalResult()
        }
        
        imageStitcher.reset()
        scrollCompensator.reset()
        
        _isProcessing.value = false
        _capturedHeight.value = 0
        _progress.value = 0f
        _statusMessage.value = "已停止"
    }
    
    fun cleanup() {
        processingScope.cancel()
        imageStitcher.reset()
        fileStorageManager.cleanupSegments()
    }
}