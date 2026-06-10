package com.example.screenshotapp.util

import android.content.Context
import android.provider.Settings

class PermissionHelper(private val context: Context) {
    
    fun canDrawOverlays(): Boolean {
        return Settings.canDrawOverlays(context)
    }
    
    fun isAccessibilityEnabled(): Boolean {
        val accessibilityEnabled = try {
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: Settings.SettingNotFoundException) {
            0
        }
        
        return accessibilityEnabled == 1
    }
    
    fun isOurAccessibilityServiceEnabled(): Boolean {
        val serviceName = "${context.packageName}/${context.packageName}.service.ScrollAccessibilityService"
        
        val enabledServices = try {
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
        } catch (e: Settings.SettingNotFoundException) {
            null
        }
        
        return enabledServices?.contains(serviceName) == true
    }
    
    fun getAccessibilityServiceSettingsIntent(): android.content.Intent {
        return android.content.Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    
    fun getOverlaySettingsIntent(): android.content.Intent {
        return android.content.Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            android.net.Uri.parse("package:${context.packageName}")
        )
    }
    
    fun hasStoragePermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            true // Android 10+ 使用 Scoped Storage，无需权限
        } else {
            context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }
}