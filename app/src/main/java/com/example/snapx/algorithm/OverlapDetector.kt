package com.example.snapx.algorithm

import android.graphics.Bitmap
import com.example.snapx.util.Constants

class OverlapDetector {
    
    fun detectOverlapByTemplate(prevBitmap: Bitmap, currBitmap: Bitmap): Int {
        val templateHeight = Constants.DEFAULT_OVERLAP_TEMPLATE_HEIGHT
        val searchHeight = Constants.DEFAULT_OVERLAP_SEARCH_HEIGHT
        
        val prevHeight = prevBitmap.height
        val currHeight = currBitmap.height
        
        val templateStartY = prevHeight - templateHeight
        val template = Bitmap.createBitmap(prevBitmap, 0, templateStartY, prevBitmap.width, templateHeight)
        
        val searchRegionHeight = minOf(searchHeight, currHeight)
        val searchRegion = Bitmap.createBitmap(currBitmap, 0, 0, currBitmap.width, searchRegionHeight)
        
        val matchOffset = TemplateMatcher.match(template, searchRegion)
        
        return matchOffset
    }
    
    fun detectOverlapByFeature(prevBitmap: Bitmap, currBitmap: Bitmap): Int {
        val edgeHeight = 50
        val prevHeight = prevBitmap.height
        
        val prevEdgeStartY = prevHeight - edgeHeight
        val prevEdge = Bitmap.createBitmap(prevBitmap, 0, prevEdgeStartY, prevBitmap.width, edgeHeight)
        
        val currEdge = Bitmap.createBitmap(currBitmap, 0, 0, currBitmap.width, edgeHeight)
        
        val prevFeatures = FeatureDetector.extractEdgeFeatures(prevEdge)
        val currFeatures = FeatureDetector.extractEdgeFeatures(currEdge)
        
        val offset = FeatureMatcher.calculateOffset(prevFeatures, currFeatures)
        
        return offset
    }
    
    fun calculateSSIM(bitmap1: Bitmap, bitmap2: Bitmap): Float {
        if (bitmap1.width != bitmap2.width || bitmap1.height != bitmap2.height) {
            return 0f
        }
        
        val width = bitmap1.width
        val height = bitmap1.height
        val pixels1 = IntArray(width * height)
        val pixels2 = IntArray(width * height)
        
        bitmap1.getPixels(pixels1, 0, width, 0, 0, width, height)
        bitmap2.getPixels(pixels2, 0, width, 0, 0, width, height)
        
        val mean1 = calculateMean(pixels1)
        val mean2 = calculateMean(pixels2)
        
        val variance1 = calculateVariance(pixels1, mean1)
        val variance2 = calculateVariance(pixels2, mean2)
        
        val covariance = calculateCovariance(pixels1, pixels2, mean1, mean2)
        
        val C1 = 6.5025
        val C2 = 58.5225
        
        val ssim = ((2 * mean1 * mean2 + C1) * (2 * covariance + C2)) /
            ((mean1 * mean1 + mean2 * mean2 + C1) * (variance1 + variance2 + C2))
        
        return ssim.toFloat()
    }
    
    private fun calculateMean(pixels: IntArray): Double {
        var sum = 0.0
        for (pixel in pixels) {
            val gray = ((pixel shr 16) & 0xFF) * 0.299 +
                       ((pixel shr 8) & 0xFF) * 0.587 +
                       (pixel & 0xFF) * 0.114
            sum += gray
        }
        return sum / pixels.size
    }
    
    private fun calculateVariance(pixels: IntArray, mean: Double): Double {
        var sum = 0.0
        for (pixel in pixels) {
            val gray = ((pixel shr 16) & 0xFF) * 0.299 +
                       ((pixel shr 8) & 0xFF) * 0.587 +
                       (pixel & 0xFF) * 0.114
            sum += (gray - mean) * (gray - mean)
        }
        return sum / pixels.size
    }
    
    private fun calculateCovariance(pixels1: IntArray, pixels2: IntArray, mean1: Double, mean2: Double): Double {
        var sum = 0.0
        for (i in pixels1.indices) {
            val gray1 = ((pixels1[i] shr 16) & 0xFF) * 0.299 +
                        ((pixels1[i] shr 8) & 0xFF) * 0.587 +
                        (pixels1[i] & 0xFF) * 0.114
            
            val gray2 = ((pixels2[i] shr 16) & 0xFF) * 0.299 +
                        ((pixels2[i] shr 8) & 0xFF) * 0.587 +
                        (pixels2[i] & 0xFF) * 0.114
            
            sum += (gray1 - mean1) * (gray2 - mean2)
        }
        return sum / pixels1.size
    }
}