import androidx.compose.material.icons.Icons.Rounded
package com.example.snapx.ui.edit

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(navController: NavController, screenshotId: Long?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "编辑截图",
                style = MaterialTheme.typography.titleLarge
            )
            
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    androidx.compose.material.icons.Icons.Rounded.Close,
                    contentDescription = "关闭"
                )
            }
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (screenshotId != null) {
                    Text(
                        "截图 ID: $screenshotId",
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Text(
                        "未选择截图",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("编辑工具", style = MaterialTheme.typography.bodyLarge)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { }) {
                        Icon(
                            androidx.compose.material.icons.Icons.Rounded.Crop,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("裁剪")
                    }
                    
                    Button(onClick = { }) {
                        Icon(
                            androidx.compose.material.icons.Icons.Rounded.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("标注")
                    }
                    
                    Button(onClick = { }) {
                        Icon(
                            androidx.compose.material.icons.Icons.Rounded.Blur,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("马赛克")
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    androidx.compose.material.icons.Icons.Rounded.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("分享")
            }
            
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f)
            ) {
                Text("保存")
            }
        }
    }
}