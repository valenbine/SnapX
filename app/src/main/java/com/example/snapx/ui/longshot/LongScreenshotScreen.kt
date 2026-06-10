package com.example.snapx.ui.longshot

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.snapx.model.PixelOption
import com.example.snapx.model.ScreenshotMode
import com.example.snapx.model.StitchAlgorithm
import com.example.snapx.viewmodel.LongScreenshotViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LongScreenshotScreen(navController: NavController) {
    val viewModel: LongScreenshotViewModel = viewModel()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "长图拼接",
            style = MaterialTheme.typography.titleLarge
        )
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("拼接模式", style = MaterialTheme.typography.bodyLarge)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = viewModel.selectedMode.value == ScreenshotMode.LONG_SCREEN_BY_SCREEN,
                        onClick = { viewModel.selectMode(ScreenshotMode.LONG_SCREEN_BY_SCREEN) },
                        label = { Text("一屏一屏") }
                    )
                    FilterChip(
                        selected = viewModel.selectedMode.value == ScreenshotMode.LONG_PIXEL_SCROLL,
                        onClick = { viewModel.selectMode(ScreenshotMode.LONG_PIXEL_SCROLL) },
                        label = { Text("像素滑动") }
                    )
                    FilterChip(
                        selected = viewModel.selectedMode.value == ScreenshotMode.LONG_AUTO_DETECT,
                        onClick = { viewModel.selectMode(ScreenshotMode.LONG_AUTO_DETECT) },
                        label = { Text("智能检测") }
                    )
                }
            }
        }
        
        if (viewModel.selectedMode.value == ScreenshotMode.LONG_PIXEL_SCROLL) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("像素参数", style = MaterialTheme.typography.bodyLarge)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            PixelOption.PIXEL_50,
                            PixelOption.PIXEL_100,
                            PixelOption.PIXEL_150
                        ).forEach { option ->
                            FilterChip(
                                selected = viewModel.pixelValue.value == option.pixel,
                                onClick = { viewModel.selectPixelOption(option) },
                                label = { Text("${option.pixel}px") }
                            )
                        }
                    }
                    
                    OutlinedTextField(
                        value = viewModel.customPixelValue.value?.toString() ?: "",
                        onValueChange = { 
                            val value = it.toIntOrNull()
                            if (value != null && value > 0) {
                                viewModel.setCustomPixelValue(value)
                            }
                        },
                        label = { Text("自定义像素值") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("拼接算法", style = MaterialTheme.typography.bodyLarge)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = viewModel.selectedAlgorithm.value == StitchAlgorithm.TEMPLATE_MATCH,
                        onClick = { viewModel.selectAlgorithm(StitchAlgorithm.TEMPLATE_MATCH) },
                        label = { Text("模板匹配") }
                    )
                    FilterChip(
                        selected = viewModel.selectedAlgorithm.value == StitchAlgorithm.FEATURE_DETECT,
                        onClick = { viewModel.selectAlgorithm(StitchAlgorithm.FEATURE_DETECT) },
                        label = { Text("特征检测") }
                    )
                }
            }
        }
        
        if (viewModel.isCapturing.value) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "正在截取长图...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "已截取: ${viewModel.capturedHeight.value} px",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = viewModel.progress.value,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(onClick = { viewModel.pauseLongScreenshot() }) {
                            Text("暂停")
                        }
                        Button(onClick = { viewModel.stopLongScreenshot() }) {
                            Text("停止")
                        }
                        Button(onClick = { viewModel.stopLongScreenshot() }) {
                            Text("保存")
                        }
                    }
                }
            }
        } else {
            Button(
                onClick = { viewModel.startLongScreenshot() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("开始长截图")
            }
        }
    }
}