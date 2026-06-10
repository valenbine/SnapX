package com.example.snapx.algorithm

import android.graphics.Bitmap

class FeatureDetector {
    
    companion object {
        fun extractEdgeFeatures(bitmap: Bitmap): List<PointFeature> {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            
            val features = mutableListOf<PointFeature>()
            
            val step = 10
            
            for (y in 1 until height - 1 step step) {
                for (x in 1 until width - 1 step step) {
                    val centerGray = getGrayValue(pixels, width, x, y)
                    val leftGray = getGrayValue(pixels, width, x - 1, y)
                    val rightGray = getGrayValue(pixels, width, x + 1, y)
                    val topGray = getGrayValue(pixels, width, x, y - 1)
                    val bottomGray = getGrayValue(pixels, width, x, y + 1)
                    
                    val horizontalDiff = Math.abs(leftGray - rightGray)
                    val verticalDiff = Math.abs(topGray - bottomGray)
                    
                    if (horizontalDiff > 30 || verticalDiff > 30) {
                        features.add(PointFeature(x, y, horizontalDiff + verticalDiff))
                    }
                }
            }
            
            return features.sortedByDescending { it.strength }.take(100)
        }
        
        private fun getGrayValue(pixels: IntArray, width: Int, x: Int, y: Int): Double {
            val pixel = pixels[y * width + x]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            
            return r * 0.299 + g * 0.587 + b * 0.114
        }
    }
}

data class PointFeature(
    val x: Int,
    val y: Int,
    val strength: Double
)