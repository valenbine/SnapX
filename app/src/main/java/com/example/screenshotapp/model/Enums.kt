package com.example.screenshotapp.model

enum class ScreenshotMode {
    NORMAL,
    LONG_SCREEN_BY_SCREEN,
    LONG_PIXEL_SCROLL,
    LONG_AUTO_DETECT
}

enum class StitchAlgorithm {
    TEMPLATE_MATCH,
    FEATURE_DETECT,
    TEMPLATE_MATCH_AUTO,
    FEATURE_DETECT_AUTO
}

enum class QualityLevel {
    HIGH,
    MEDIUM,
    LOW
}

enum class PixelOption(val pixel: Int) {
    PIXEL_50(50),
    PIXEL_100(100),
    PIXEL_150(150),
    PIXEL_CUSTOM(0) // 自定义值，需要用户输入
}