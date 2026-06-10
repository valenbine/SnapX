package com.example.snapx.ui.floating

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import com.example.snapx.util.PermissionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FloatingWindowManager(private val context: Context) {
    
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val permissionHelper = PermissionHelper(context)
    
    private val _isVisible = MutableStateFlow(false)
    val isVisible: StateFlow<Boolean> = _isVisible
    
    private var floatingView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    
    private val _isExpanded = MutableStateFlow(false)
    val isExpanded: StateFlow<Boolean> = _isExpanded
    
    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing
    
    private val _capturedHeight = MutableStateFlow(0)
    val capturedHeight: StateFlow<Int> = _capturedHeight
    
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress
    
    fun canShowFloatingWindow(): Boolean {
        return permissionHelper.canDrawOverlays()
    }
    
    fun showFloatingWindow() {
        if (!canShowFloatingWindow()) {
            return
        }
        
        if (_isVisible.value) {
            return
        }
        
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.LEFT
            x = 100
            y = 100
        }
        
        floatingView = createFloatingView()
        
        windowManager.addView(floatingView, layoutParams)
        
        _isVisible.value = true
    }
    
    fun hideFloatingWindow() {
        if (!_isVisible.value) {
            return
        }
        
        if (floatingView != null) {
            windowManager.removeView(floatingView)
            floatingView = null
        }
        
        _isVisible.value = false
        _isExpanded.value = false
    }
    
    fun expandMenu() {
        _isExpanded.value = true
        updateViewLayout()
    }
    
    fun collapseMenu() {
        _isExpanded.value = false
        updateViewLayout()
    }
    
    fun updateCapturingProgress(height: Int, progressValue: Float) {
        _isCapturing.value = true
        _capturedHeight.value = height
        _progress.value = progressValue
        updateViewLayout()
    }
    
    fun stopCapturing() {
        _isCapturing.value = false
        _capturedHeight.value = 0
        _progress.value = 0f
        updateViewLayout()
    }
    
    private fun createFloatingView(): View {
        val container = FrameLayout(context)
        
        container.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        
        return container
    }
    
    private fun updateViewLayout() {
        if (floatingView != null && layoutParams != null) {
            val newHeight = if (_isExpanded.value) {
                WindowManager.LayoutParams.WRAP_CONTENT
            } else {
                60
            }
            
            layoutParams?.height = newHeight
            
            windowManager.updateViewLayout(floatingView, layoutParams)
        }
    }
    
    fun moveFloatingWindow(x: Int, y: Int) {
        if (layoutParams != null && _isVisible.value) {
            layoutParams?.x = x
            layoutParams?.y = y
            windowManager.updateViewLayout(floatingView, layoutParams)
        }
    }
    
    fun cleanup() {
        hideFloatingWindow()
    }
}