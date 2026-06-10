package com.example.snapx.algorithm

import android.graphics.Bitmap

class TemplateMatcher {
    
    companion object {
        fun match(template: Bitmap, searchRegion: Bitmap): Int {
            val templateWidth = template.width
            val templateHeight = template.height
            val searchWidth = searchRegion.width
            val searchHeight = searchRegion.height
            
            val templatePixels = IntArray(templateWidth * templateHeight)
            template.getPixels(templatePixels, 0, templateWidth, 0, 0, templateWidth, templateHeight)
            
            val searchPixels = IntArray(searchWidth * searchHeight)
            searchRegion.getPixels(searchPixels, 0, searchWidth, 0, 0, searchWidth, searchHeight)
            
            var minDiff = Double.MAX_VALUE
            var bestOffset = -1
            
            val maxSearchY = searchHeight - templateHeight
            
            for (offsetY in 0..maxSearchY) {
                val diff = calculateDifference(templatePixels, searchPixels, templateWidth, templateHeight, searchWidth, offsetY)
                
                if (diff < minDiff) {
                    minDiff = diff
                    bestOffset = offsetY
                }
                
                if (minDiff < 10.0) {
                    break
                }
            }
            
            return bestOffset
        }
        
        private fun calculateDifference(
            templatePixels: IntArray,
            searchPixels: IntArray,
            templateWidth: Int,
            templateHeight: Int,
            searchWidth: Int,
            offsetY: Int
        ): Double {
            var totalDiff = 0.0
            
            for (y in 0 until templateHeight) {
                for (x in 0 until templateWidth) {
                    val templateIndex = y * templateWidth + x
                    val searchIndex = (offsetY + y) * searchWidth + x
                    
                    val templatePixel = templatePixels[templateIndex]
                    val searchPixel = searchPixels[searchIndex]
                    
                    val templateGray = getGrayValue(templatePixel)
                    val searchGray = getGrayValue(searchPixel)
                    
                    val diff = Math.abs(templateGray - searchGray)
                    totalDiff += diff
                }
            }
            
            return totalDiff / (templateWidth * templateHeight)
        }
        
        private fun getGrayValue(pixel: Int): Double {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            
            return r * 0.299 + g * 0.587 + b * 0.114
        }
    }
}