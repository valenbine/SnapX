package com.example.snapx.model

data class LongScreenshotConfig(
    val mode: ScreenshotMode,
    val pixelValue: Int,
    val algorithm: StitchAlgorithm,
    val maxHeight: Int? = null,
    val autoStopThreshold: Float = 0.95f,
    val retryCount: Int = 3,
    val delayMs: Long = 300
) {
    companion object {
        val DEFAULT = LongScreenshotConfig(
            mode = ScreenshotMode.LONG_PIXEL_SCROLL,
            pixelValue = 100,
            algorithm = StitchAlgorithm.TEMPLATE_MATCH,
            maxHeight = 10000,
            autoStopThreshold = 0.95f,
            retryCount = 3,
            delayMs = 300
        )
    }
}