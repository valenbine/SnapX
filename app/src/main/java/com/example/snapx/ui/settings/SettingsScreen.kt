package com.example.snapx.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.snapx.model.PixelOption
import com.example.snapx.model.QualityLevel
import com.example.snapx.model.StitchAlgorithm
import com.example.snapx.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val viewModel: SettingsViewModel = viewModel()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.titleLarge
        )
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("默认像素值", style = MaterialTheme.typography.bodyLarge)
                
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
                            selected = viewModel.defaultPixel.value == option.pixel,
                            onClick = { viewModel.setDefaultPixel(option.pixel) },
                            label = { Text("${option.pixel}px") }
                        )
                    }
                }
            }
        }
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("默认拼接算法", style = MaterialTheme.typography.bodyLarge)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = viewModel.defaultAlgorithm.value == StitchAlgorithm.TEMPLATE_MATCH,
                        onClick = { viewModel.setDefaultAlgorithm(StitchAlgorithm.TEMPLATE_MATCH) },
                        label = { Text("模板匹配") }
                    )
                    FilterChip(
                        selected = viewModel.defaultAlgorithm.value == StitchAlgorithm.FEATURE_DETECT,
                        onClick = { viewModel.setDefaultAlgorithm(StitchAlgorithm.FEATURE_DETECT) },
                        label = { Text("特征检测") }
                    )
                }
            }
        }
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("截图质量", style = MaterialTheme.typography.bodyLarge)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        QualityLevel.HIGH,
                        QualityLevel.MEDIUM,
                        QualityLevel.LOW
                    ).forEach { level ->
                        FilterChip(
                            selected = viewModel.qualityLevel.value == level,
                            onClick = { viewModel.setQualityLevel(level) },
                            label = { Text(level.name) }
                        )
                    }
                }
            }
        }
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("悬浮窗", style = MaterialTheme.typography.bodyLarge)
                
                Switch(
                    checked = viewModel.floatingWindowEnabled.value,
                    onCheckedChange = { viewModel.setFloatingWindowEnabled(it) }
                )
            }
        }
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("定期清理", style = MaterialTheme.typography.bodyLarge)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(7, 14, 30).forEach { days ->
                        FilterChip(
                            selected = viewModel.autoCleanDays.value == days,
                            onClick = { viewModel.setAutoCleanDays(days) },
                            label = { Text("${days}天") }
                        )
                    }
                }
            }
        }
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("存储路径", style = MaterialTheme.typography.bodyLarge)
                
                OutlinedTextField(
                    value = viewModel.storagePath.value,
                    onValueChange = { viewModel.setStoragePath(it) },
                    label = { Text("存储路径") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    }
}