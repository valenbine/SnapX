package com.example.snapx.algorithm

class FeatureMatcher {
    
    companion object {
        fun calculateOffset(features1: List<PointFeature>, features2: List<PointFeature>): Int {
            if (features1.isEmpty() || features2.isEmpty()) {
                return -1
            }
            
            val matchThreshold = 50.0
            val maxOffsetY = 200
            
            var bestOffset = -1
            var bestMatchCount = 0
            
            for (offsetY in 0..maxOffsetY) {
                var matchCount = 0
                
                for (feature1 in features1) {
                    val adjustedY = feature1.y + offsetY
                    
                    for (feature2 in features2) {
                        val yDiff = Math.abs(adjustedY - feature2.y)
                        val xDiff = Math.abs(feature1.x - feature2.x)
                        val strengthDiff = Math.abs(feature1.strength - feature2.strength)
                        
                        if (yDiff < 5 && xDiff < 5 && strengthDiff < matchThreshold) {
                            matchCount++
                            break
                        }
                    }
                }
                
                if (matchCount > bestMatchCount) {
                    bestMatchCount = matchCount
                    bestOffset = offsetY
                }
                
                if (bestMatchCount >= features1.size * 0.8) {
                    break
                }
            }
            
            return if (bestMatchCount >= features1.size * 0.5) {
                bestOffset
            } else {
                -1
            }
        }
    }
}