package com.example.screenshotapp.util

object Constants {
    
    const val REQUEST_MEDIA_PROJECTION = 1001
    const val REQUEST_OVERLAY_PERMISSION = 1002
    const val REQUEST_STORAGE_PERMISSION = 1003
    
    const val NOTIFICATION_ID_SCREENSHOT_SERVICE = 1001
    const val NOTIFICATION_ID_LONG_SCREENSHOT = 1002
    
    const val DEFAULT_OVERLAP_TEMPLATE_HEIGHT = 100
    const val DEFAULT_OVERLAP_SEARCH_HEIGHT = 200
    const val DEFAULT_SIMILARITY_THRESHOLD = 0.95f
    
    const val DEFAULT_SCROLL_DELAY_MS = 300L
    const val DEFAULT_FRAME_RATE_THRESHOLD = 10
    
    const val MAX_SEGMENT_CACHE_COUNT = 10
    const val MAX_SEGMENT_MEMORY_SIZE = 10 * 1024 * 1024 // 10MB
    
    const val MAX_RETRY_COUNT = 3
    const val RETRY_DELAY_MS = 100L
    
    val PIXEL_OPTIONS = listOf(50, 100, 150, 200, 250)
    
    val CLEANUP_DAYS_OPTIONS = listOf(7, 14, 30)
    
    const val MAX_LONG_SCREENSHOT_HEIGHT_DEFAULT = 10000
    
    const val MIN_SDK_FOR_MEDIA_PROJECTION = 21 // Android 5.0
    const val MIN_SDK_FOR_ACCESSIBILITY_GESTURES = 24 // Android 7.0
}