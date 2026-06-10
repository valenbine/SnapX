package com.example.snapx.ui.floating

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.snapx.R

class FloatingWindowUI(private val context: Context) {
    
    private val inflater = LayoutInflater.from(context)
    
    fun createCollapsedView(): View {
        val container = LinearLayout(context)
        container.orientation = LinearLayout.VERTICAL
        
        val layoutParams = LinearLayout.LayoutParams(60, 60)
        layoutParams.gravity = android.view.Gravity.CENTER
        
        val ballView = ImageView(context)
        ballView.layoutParams = layoutParams
        ballView.setImageDrawable(ContextCompat.getDrawable(context, android.R.drawable.ic_menu_camera))
        ballView.setBackgroundResource(android.R.drawable.sym_def_app_icon)
        
        container.addView(ballView)
        
        return container
    }
    
    fun createExpandedView(): View {
        val container = LinearLayout(context)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(16, 16, 16, 16)
        
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 8, 0, 8)
        
        val titleText = TextView(context)
        titleText.text = "X截图"
        titleText.setTextSize(16f)
        titleText.setTextColor(ContextCompat.getColor(context, android.R.color.black))
        titleText.layoutParams = layoutParams
        container.addView(titleText)
        
        val normalButton = createButton("普通截图")
        normalButton.layoutParams = layoutParams
        container.addView(normalButton)
        
        val longButton = createButton("长截图")
        longButton.layoutParams = layoutParams
        container.addView(longButton)
        
        val closeButton = createButton("关闭")
        closeButton.layoutParams = layoutParams
        container.addView(closeButton)
        
        return container
    }
    
    fun createCapturingView(height: Int, progress: Float): View {
        val container = LinearLayout(context)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(16, 16, 16, 16)
        
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 8, 0, 8)
        
        val titleText = TextView(context)
        titleText.text = "正在截取长图..."
        titleText.setTextSize(14f)
        titleText.setTextColor(ContextCompat.getColor(context, android.R.color.black))
        titleText.layoutParams = layoutParams
        container.addView(titleText)
        
        val heightText = TextView(context)
        heightText.text = "已截取: $height px"
        heightText.setTextSize(12f)
        heightText.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
        heightText.layoutParams = layoutParams
        container.addView(heightText)
        
        val progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
        progressBar.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            20
        )
        progressBar.progress = (progress * 100).toInt()
        container.addView(progressBar)
        
        val buttonContainer = LinearLayout(context)
        buttonContainer.orientation = LinearLayout.HORIZONTAL
        buttonContainer.layoutParams = layoutParams
        
        val pauseButton = createButton("暂停")
        buttonContainer.addView(pauseButton)
        
        val stopButton = createButton("停止")
        buttonContainer.addView(stopButton)
        
        val saveButton = createButton("保存")
        buttonContainer.addView(saveButton)
        
        container.addView(buttonContainer)
        
        return container
    }
    
    private fun createButton(text: String): TextView {
        val button = TextView(context)
        button.text = text
        button.setTextSize(14f)
        button.setTextColor(ContextCompat.getColor(context, android.R.color.black))
        button.setBackgroundResource(android.R.drawable.btn_default)
        button.setPadding(8, 8, 8, 8)
        button.gravity = android.view.Gravity.CENTER
        
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        layoutParams.setMargins(4, 0, 4, 0)
        button.layoutParams = layoutParams
        
        return button
    }
}