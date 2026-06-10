package com.example.snapx.ui.screenshot

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons.Rounded
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snapx.R
import com.example.snapx.model.ScreenshotMode
import com.example.snapx.viewmodel.ScreenshotViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Intent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ScreenshotScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: ScreenshotViewModel = viewModel()
    
    val mediaProjectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            viewModel.handleMediaProjectionResult(result.resultCode, result.data!!)
        }
    }
    
    var selectedDelay by remember { mutableStateOf(0) }
    val delayOptions = listOf(0, 3, 5, 10)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "普通截图",
            style = MaterialTheme.typography.titleLarge
        )
        
        if (!viewModel.hasMediaProjectionPermission.value) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "需要屏幕投影权限",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        val manager = context.getSystemService(android.content.Context.MEDIA_PROJECTION_SERVICE)
                            as android.media.projection.MediaProjectionManager
                        mediaProjectionLauncher.launch(manager.createScreenCaptureIntent())
                    }) {
                        Text("授权")
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.captureFullScreen(selectedDelay) }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Rounded.PhotoCamera,
                        contentDescription = "整屏截图",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("整屏截图", style = MaterialTheme.typography.bodyLarge)
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.captureArea(selectedDelay) }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Rounded.Crop,
                        contentDescription = "区域截图",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("区域截图", style = MaterialTheme.typography.bodyLarge)
                }
            }
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("延时截图", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        delayOptions.forEach { delay ->
                            FilterChip(
                                selected = selectedDelay == delay,
                                onClick = { selectedDelay = delay },
                                label = { Text(if (delay == 0) "立即" else "${delay}秒") }
                            )
                        }
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
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("正在截图...", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            
            if (viewModel.lastScreenshot.value != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navController.navigate("edit/${viewModel.lastScreenshotId}")
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("截图完成", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(onClick = { viewModel.shareScreenshot() }) {
                                Text("分享")
                            }
                            Button(onClick = { navController.navigate("edit/${viewModel.lastScreenshotId}") }) {
                                Text("编辑")
                            }
                        }
                    }
                }
            }
        }
    }
}