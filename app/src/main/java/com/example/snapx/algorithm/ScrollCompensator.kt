package com.example.snapx.algorithm

class ScrollCompensator {
    
    private var accumulatedError = 0
    private val compensationThreshold = 5
    
    private val recentOffsets = mutableListOf<Int>()
    private val maxHistorySize = 10
    
    fun calculateNextScroll(userPixel: Int, actualOffset: Int): Int {
        val deviation = actualOffset - userPixel
        
        recentOffsets.add(actualOffset)
        if (recentOffsets.size > maxHistorySize) {
            recentOffsets.removeAt(0)
        }
        
        accumulatedError += deviation
        
        val correction = calculateCorrection()
        
        val nextScroll = userPixel + correction
        
        return nextScroll.coerceAtLeast(1).coerceAtMost(300)
    }
    
    private fun calculateCorrection(): Int {
        if (Math.abs(accumulatedError) < compensationThreshold) {
            return 0
        }
        
        val averageOffset = if (recentOffsets.isNotEmpty()) {
            recentOffsets.sum() / recentOffsets.size
        } else {
            0
        }
        
        val correction = -accumulatedError
        
        accumulatedError = 0
        
        return correction
    }
    
    fun getAccumulatedError(): Int {
        return accumulatedError
    }
    
    fun getAverageOffset(): Int {
        return if (recentOffsets.isNotEmpty()) {
            recentOffsets.sum() / recentOffsets.size
        } else {
            0
        }
    }
    
    fun reset() {
        accumulatedError = 0
        recentOffsets.clear()
    }
    
    fun getStatistics(): CompensatorStatistics {
        return CompensatorStatistics(
            accumulatedError = accumulatedError,
            averageOffset = getAverageOffset(),
            recentCount = recentOffsets.size
        )
    }
}

data class CompensatorStatistics(
    val accumulatedError: Int,
    val averageOffset: Int,
    val recentCount: Int
)