package com.example.screenshotapp.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ScrollAccessibilityService : AccessibilityService() {
    
    private val _isScrolling = MutableStateFlow(false)
    val isScrolling: StateFlow<Boolean> = _isScrolling
    
    private val _scrollCompleted = MutableStateFlow(false)
    val scrollCompleted: StateFlow<Boolean> = _scrollCompleted
    
    private val handler = Handler(Looper.getMainLooper())
    
    companion object {
        private var instance: ScrollAccessibilityService? = null
        
        fun getInstance(): ScrollAccessibilityService? {
            return instance
        }
        
        fun isServiceEnabled(): Boolean {
            return instance != null
        }
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 不处理事件，主要用于执行滚动操作
    }
    
    override fun onInterrupt() {
        // 处理中断
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
    
    fun scrollByPixels(pixel: Int, duration: Long = 300): Boolean {
        val rootView = rootInActiveWindow
        if (rootView == null) {
            return false
        }
        
        val scrollableNode = findScrollableNode(rootView)
        if (scrollableNode != null) {
            return scrollNodeByPixels(scrollableNode, pixel)
        }
        
        return performGestureScroll(pixel, duration)
    }
    
    fun scrollFullScreen(duration: Long = 300): Boolean {
        val rootView = rootInActiveWindow
        if (rootView == null) {
            return false
        }
        
        val screenHeight = getScreenHeight(rootView)
        return performGestureScroll(screenHeight, duration)
    }
    
    private fun findScrollableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable()) {
            return node
        }
        
        val childCount = node.childCount
        for (i in 0 until childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val scrollable = findScrollableNode(child)
                if (scrollable != null) {
                    return scrollable
                }
            }
        }
        
        return null
    }
    
    private fun scrollNodeByPixels(node: AccessibilityNodeInfo, pixels: Int): Boolean {
        _isScrolling.value = true
        _scrollCompleted.value = false
        
        val result = node.performAction(
            AccessibilityNodeInfo.ACTION_SCROLL_FORWARD,
            android.os.Bundle()
        )
        
        handler.postDelayed({
            _isScrolling.value = false
            _scrollCompleted.value = true
        }, 300)
        
        return result
    }
    
    private fun performGestureScroll(distance: Int, duration: Long): Boolean {
        _isScrolling.value = true
        _scrollCompleted.value = false
        
        val path = Path()
        val screenHeight = resources.displayMetrics.heightPixels
        val screenWidth = resources.displayMetrics.widthPixels
        
        val startX = screenWidth / 2f
        val startY = screenHeight * 0.8f
        val endY = startY - distance
        
        path.moveTo(startX, startY)
        path.lineTo(startX, endY)
        
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()
        
        val result = dispatchGesture(gesture, null, null)
        
        handler.postDelayed({
            _isScrolling.value = false
            _scrollCompleted.value = true
        }, duration + 50)
        
        return result
    }
    
    private fun getScreenHeight(node: AccessibilityNodeInfo): Int {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        return bounds.height()
    }
    
    fun isAtBottom(): Boolean {
        val rootView = rootInActiveWindow
        if (rootView == null) {
            return false
        }
        
        val scrollableNode = findScrollableNode(rootView)
        if (scrollableNode != null) {
            val bounds = Rect()
            scrollableNode.getBoundsInScreen(bounds)
            
            val scrollRange = scrollableNode.scrollRange()
            val scrollY = scrollableNode.scrollY()
            
            return scrollY >= scrollRange - 10
        }
        
        return false
    }
}