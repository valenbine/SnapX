# Android 截图工具设计方案

**日期**: 2026-06-10  
**项目名称**: Android LongScreenshot Tool  
**目标**: 开发一个功能完善的 Android 截图工具，支持普通截图和多种长图拼接模式

---

## 1. 项目概述

### 1.1 功能需求

**核心功能**:
- 普通截图：整屏截图、区域截图、延时截图
- 长图拼接（三种模式）:
  1. **一屏一屏拼接**: 通过 AccessibilityService 实现整屏滚动，每次截取整屏内容后拼接
  2. **像素级滑动拼接**: 每次滑动固定像素高度（用户可自定义），连续截图并智能拼接，确保完美无重复无断裂
  3. **智能自动边界检测**: 自动检测页面底部，无需用户干预即可完成长图截图

**特色功能**:
- 用户自定义像素级滑动参数（档位 + 自定义输入）
- 实时进度显示和拼接预览
- 截图编辑（裁剪、标注、马赛克）
- 悬浮窗快捷操作
- 历史记录管理

### 1.2 目标用户

- 需要截取聊天记录、长网页、长文档的普通用户
- 需要精确控制截图质量的技术用户
- 需要快速截图操作的高频用户

---

## 2. 技术栈

### 2.1 核心技术

**开发语言**: Kotlin  
**UI 框架**: Jetpack Compose  
**目标平台**: Android 14 (API 34)  
**最低兼容**: Android 5.0 (API 21) - MediaProjection API 最低要求  

### 2.2 Jetpack 组件清单

| 组件 | 用途 |
|------|------|
| **Compose** | UI 界面开发，声明式 UI |
| **ViewModel** | MVVM 架构的 ViewModel 层，管理 UI 状态 |
| **LiveData** | 向 UI 层发送状态更新（可选，也可用 StateFlow） |
| **Room** | 截图记录的数据库存储 |
| **Coroutines** | 异步截图和图像拼接处理 |
| **DataStore** | 用户配置参数的持久化存储 |
| **Lifecycle** | 管理 UI 生命周期和权限请求时机 |
| **Navigation Compose** | 底部导航和页面切换 |
| **WorkManager** | 后台定时清理临时文件的任务 |

### 2.3 第三方库

| 库 | 用途 |
|------|------|
| **Accompanist** | Compose 的权限请求辅助库 |
| **Coil** | 图片加载和缩略图显示 |
| **OpenCV (可选)** | 图像处理和拼接算法（如模板匹配） |
| **LeakCanary** | 内存泄漏检测（开发调试） |

---

## 3. 架构设计

### 3.1 MVVM 架构

采用标准的 MVVM 架构模式：

```
┌─────────────────┐
│  UI Layer       │
│  (Compose)      │
│  - MainScreen   │
│  - FloatingWindow│
│  - SettingsScreen│
└────────┬────────┘
         │ observe State/LiveData
┌────────▼────────┐
│  ViewModel      │
│  - ScreenshotVM │
│  - LongScreenshotVM│
│  - SettingsVM   │
└────────┬────────┘
         │ call Repository/Service
┌────────▼────────┐
│  Business Layer │
│  - Repository   │
│  - Service      │
└────────┬────────┘
         │ access Data/API
┌────────▼────────┐
│  Data Layer     │
│  - Room DB      │
│  - DataStore    │
│  - FileStorage  │
└─────────────────┘
```

### 3.2 数据流向

**单向数据流**:
1. UI 层发送用户意图 (Intent) 到 ViewModel
2. ViewModel 处理意图，调用 Repository/Service
3. Repository/Service 返回结果或状态
4. ViewModel 更新 State/LiveData
5. UI 层观察 State 变化并自动更新

**意图示例**:
```kotlin
sealed class ScreenshotIntent {
    object StartNormalScreenshot : ScreenshotIntent()
    object StartLongScreenshot : ScreenshotIntent()
    data class SetScrollPixel(val pixel: Int) : ScreenshotIntent()
    object StopLongScreenshot : ScreenshotIntent()
}
```

---

## 4. 核心模块划分

### 4.1 模块结构

```
app/
├── ui/                          # UI 层
│   ├── main/                    # 主界面
│   ├── floating/                # 悬浮窗
│   ├── settings/                # 设置界面
│   └── components/              # 共享 UI 组件
├── viewmodel/                   # ViewModel 层
│   ├── ScreenshotViewModel.kt
│   ├── LongScreenshotViewModel.kt
│   └── SettingsViewModel.kt
├── service/                     # 业务服务层
│   ├── ScreenshotService.kt     # 截图核心服务
│   ├── MediaProjectionManager.kt# MediaProjection 管理
│   ├── AccessibilityService.kt  # 无障碍服务（滚动控制）
│   ├── LongScreenshotProcessor.kt# 长图拼接处理器
│   ├── ScrollController.kt      # 滚动控制器
│   └── ImageStitcher.kt         # 图像拼接引擎
├── repository/                  # 数据仓库层
│   ├── ScreenshotRepository.kt
│   └── SettingsRepository.kt
├── data/                        # 数据层
│   ├── database/                # Room 数据库
│   │   ├── ScreenshotDao.kt
│   │   ├── ScreenshotEntity.kt
│   │   └── AppDatabase.kt
│   ├── datastore/               # DataStore 配置存储
│   │   └── SettingsDataStore.kt
│   └── storage/                 # 文件存储
│   │   ├── FileStorageManager.kt
│   │   └── TempFileManager.kt
├── algorithm/                   # 图像处理算法
│   ├── OverlapDetector.kt       # 重叠区域检测
│   ├── TemplateMatcher.kt       # 模板匹配
│   ├── FeatureDetector.kt       # 特征点检测
│   └── StitchAlgorithm.kt       # 拼接算法
├── util/                        # 工具类
│   ├── PermissionHelper.kt      # 权限辅助
│   ├── ImageUtils.kt            # 图像处理工具
│   ├── Constants.kt             # 常量定义
└── model/                       # 数据模型
    ├── ScreenshotMode.kt        # 截图模式枚举
    ├── LongScreenshotConfig.kt  # 长图配置
    ├── ScreenshotRecord.kt      # 截图记录
```

### 4.2 模块职责

**ScreenshotService** - 截图服务核心
- 管理 MediaProjection 权限和虚拟显示
- 实现单张截图捕获（VirtualDisplay + ImageReader）
- 提供截图回调接口

**LongScreenshotProcessor** - 长图拼接核心
- 协调滚动控制器和图像拼接引擎
- 维护拼接状态和进度
- 处理三种拼接模式的切换

**ImageStitcher** - 图像拼接引擎
- 实现一屏拼接算法（简单拼接）
- 实现像素级拼接算法（智能裁剪）
- 维护拼接缓冲区和临时文件

---

## 5. 截长图三种模式的详细流程

### 5.1 模式一：一屏一屏拼接

```
[用户启动] 
    ↓
[AccessibilityService 执行整屏滚动]
    ↓ scrollBy(viewHeight)
[等待页面稳定]
    ↓ delay(300ms) or frameRate < threshold
[截取整屏]
    ↓ VirtualDisplay.capture()
[检测最后一行]
    ↓ 作为下次拼接起点标记
[简单拼接]
    ↓ ImageStitcher.simpleStitch()
[循环判断]
    ↓ 用户停止 or 检测到底部 → 结束
    ↓ 否则 → 继续滚动
```

**关键点**:
- 滚动距离：每次滚动一个屏幕高度（需要获取目标 View 的高度）
- 等待稳定：检测帧率变化或固定延时 300-500ms
- 拼接起点：截取最后一行内容作为下次比对基准

### 5.2 模式二：像素级滑动拼接

```
[用户设置像素值 (如 100px)]
    ↓
[启动截图] 
    ↓
[循环开始]
    ↓
[滑动固定像素]
    ↓ scrollBy(userPixel) or gestureScroll(pixel)
[立即截图]
    ↓ capture() → 存入 temp buffer
[检测实际重叠区域]
    ↓ OverlapDetector.detectOverlap(prevBottom, currTop)
[计算偏差]
    ↓ actualOffset - userPixel = deviation
[精确裁剪重叠部分]
    ↓ crop overlap region from current screenshot
[拼接新片段]
    ↓ ImageStitcher.stitchSegment(croppedSegment)
[偏差补偿]
    ↓ nextScrollPixel = userPixel + deviationCorrection
[循环判断]
    ↓ 用户停止 or 完成高度 → 结束
    ↓ 否则 → 继续滑动
```

**关键算法**:
1. **重叠检测**: 使用模板匹配算法，将前一截图底部 50-100px 作为模板，在后一截图顶部搜索匹配位置
2. **偏差补偿**: 每次截图后计算实际偏移量和预设值的偏差，累积误差并在下次滚动中补偿
3. **防断裂机制**: 如果检测到拼接断裂（重叠区域未找到），触发重试最近一次滚动截图

### 5.3 模式三：智能自动边界检测

```
[用户启动]
    ↓
[自动开始滚动]
    ↓ AccessibilityService 或手势模拟
[连续截图]
    ↓ 每次滚动 100-200px (固定或自适应)
[实时比对相邻截图]
    ↓ OverlapDetector.compare(prevBottom, currTop)
[检测边界]
    ↓ similarity > threshold (如 95%) → 判断到底部
    ↓ 或 contentHeight 未变化 → 判断到底部
[自动停止]
    ↓ 停止滚动 → 完成拼接 → 保存
```

**边界检测算法**:
- **相似度阈值**: 使用 SSIM (结构相似度) 算法，当相邻截图的底部和顶部相似度 > 95% 时判断为到底部
- **内容高度检测**: 监控截取的总高度变化，如果连续 3 次截图高度不变，判断为到底部
- **停止条件**: 自动停止 + 用户手动停止（双保险）

---

## 6. 像素级完美拼接的核心算法

### 6.1 重叠区域检测算法

**算法一：模板匹配 (Template Matching)**
```kotlin
fun detectOverlapByTemplate(prevBitmap: Bitmap, currBitmap: Bitmap): Int {
    val templateHeight = 100 // 取前一截图底部 100px 作为模板
    val template = prevBitmap.extractBottom(templateHeight)
    
    // 在当前截图顶部搜索模板位置
    val searchRegion = currBitmap.extractTop(prevBitmap.height)
    val matchResult = TemplateMatcher.match(template, searchRegion)
    
    return matchResult.offset // 返回重叠像素数
}
```

**算法二：特征点比对 (Feature Detection)**
```kotlin
fun detectOverlapByFeature(prevBitmap: Bitmap, currBitmap: Bitmap): Int {
    val prevFeatures = FeatureDetector.extractEdgeFeatures(prevBitmap.bottom)
    val currFeatures = FeatureDetector.extractEdgeFeatures(currBitmap.top)
    
    val offset = FeatureMatcher.calculateOffset(prevFeatures, currFeatures)
    return offset
}
```

### 6.2 滑动距离补偿算法

```kotlin
class ScrollCompensator {
    private var accumulatedError = 0
    private val compensationThreshold = 5 // 像素
    
    fun calculateNextScroll(userPixel: Int, actualOffset: Int): Int {
        val deviation = actualOffset - userPixel
        accumulatedError += deviation
        
        // 累积误差超过阈值时进行补偿
        if (abs(accumulatedError) > compensationThreshold) {
            val compensation = accumulatedError
            accumulatedError = 0 // 清零累积误差
            return userPixel + compensation
        }
        
        return userPixel
    }
}
```

### 6.3 防断裂机制

```kotlin
fun stitchWithRetry(prevSegment: Bitmap, currSegment: Bitmap): StitchResult {
    val overlap = OverlapDetector.detectOverlap(prevSegment, currSegment)
    
    // 检测失败（重叠区域未找到）
    if (overlap == -1) {
        // 重试机制：回滚最近 1 次截图
        return StitchResult.Failure(needRetry = true)
    }
    
    // 检测成功
    val croppedSegment = currSegment.cropTop(overlap)
    val stitchedBitmap = prevSegment.stitch(croppedSegment)
    
    return StitchResult.Success(stitchedBitmap)
}
```

### 6.4 防重复机制

```kotlin
fun verifyNoDuplicate(prevSegment: Bitmap, croppedSegment: Bitmap): Boolean {
    // 比对 prevSegment 的底部和 croppedSegment 的顶部
    val prevBottom = prevSegment.extractBottom(20)
    val croppedTop = croppedSegment.extractTop(20)
    
    val similarity = calculateSSIM(prevBottom, croppedTop)
    
    // 相似度应该接近 100%，表示完全去重
    return similarity > 98 // 允许 2% 的误差
}
```

---

## 7. UI 设计详细说明

### 7.1 主界面 (MainScreen)

**底部导航结构**:
```
┌─────────────────────────────────────┐
│     [底部导航栏]                     │
│  ┌──────┬──────┬──────┬──────┐     │
│  │截图  │长截图│历史  │设置  │     │
│  └──────┴──────┴──────┴──────┘     │
└─────────────────────────────────────┘
```

**截图页 (ScreenshotScreen)**:
```
┌─────────────────────────────────────┐
│  普通截图                            │
│  ┌─────────────────────────────┐   │
│  │  [整屏截图按钮]               │   │
│  │  [区域截图按钮]               │   │
│  │  [延时截图: 3s / 5s / 10s]   │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

**长截图页 (LongScreenshotScreen)**:
```
┌─────────────────────────────────────┐
│  长图拼接                            │
│  ┌─────────────────────────────┐   │
│  │  □ 一屏一屏拼接              │   │
│  │  □ 像素级滑动拼接            │   │
│  │  □ 智能自动检测              │   │
│  └─────────────────────────────┘   │
│  ┌─────────────────────────────┐   │
│  │  参数设置                    │   │
│  │  滑动像素: [50][100][150]    │   │
│  │            [自定义输入框]    │   │
│  │  拼接算法: [模板匹配]        │   │
│  │            [特征点检测]      │   │
│  └─────────────────────────────┘   │
│  [开始长截图]                       │
└─────────────────────────────────────┘
```

**历史记录页 (HistoryScreen)**:
```
┌─────────────────────────────────────┐
│  截图历史                            │
│  ┌─────────────────────────────┐   │
│  │  [截图缩略图列表]            │   │
│  │  - 点击查看详情              │   │
│  │  - 长按删除                  │   │
│  │  - 分享/编辑按钮             │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

**设置页 (SettingsScreen)**:
```
┌─────────────────────────────────────┐
│  设置                                │
│  ┌─────────────────────────────┐   │
│  │  默认像素值: 50/100/150      │   │
│  │  默认拼接算法: 模板匹配      │   │
│  │  截图质量: 高/中/低          │   │
│  │  存储路径: [选择目录]        │   │
│  │  悬浮窗开关: ON/OFF          │   │
│  │  定期清理: 7天/14天/30天     │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

### 7.2 悬浮窗 (FloatingWindow)

**小圆球形态**:
```
    ┌───┐
    │ ⊙ │  ← 点击展开菜单
    └───┘
```

**展开后的快捷菜单**:
```
┌─────────────┐
│  普通截图    │
│  长截图      │
│  最近设置    │
│  关闭        │
└─────────────┘
```

**长截图进行中的状态**:
```
┌─────────────────────────────┐
│  正在截取长图...             │
│  已截取: 3000px              │
│  ┌───────────────────────┐ │
│  │  ████████░░░░░░░ 60%  │ │  ← 进度条
│  └───────────────────────┘ │
│  [暂停] [停止] [保存]        │
└─────────────────────────────┘
```

### 7.3 长截图进行时的实时预览

```
┌─────────────────────────────────────┐
│  实时预览                            │
│  ┌─────────────────────────────┐   │
│  │  [当前拼接后的长图缩略图]    │   │
│  │  - 可上下滚动查看            │   │
│  │  - 标记拼接点位置            │   │
│  └─────────────────────────────┘   │
│  进度: 3000px / 目标 5000px         │
│  [调整像素值: 50 → 100]             │
└─────────────────────────────────────┘
```

---

## 8. 数据存储方案

### 8.1 截图存储结构

**目录结构**:
```
/storage/emulated/0/ScreenshotApp/
├── normal/                   # 普通截图目录
│   ├── 20260610_143025_normal.png
│   ├── 20260610_150130_normal.png
│   └── ...
├── long/                     # 长图目录
│   ├── 20260610_143025_long.png
│   ├── 20260610_150130_long.png
│   └── ...
└── temp/                     # 临时文件目录
    ├── segments/             # 拼接过程中的分段图片
    │   ├── segment_001.png
    │   ├── segment_002.png
    │   └── ...
    └── cache/                # 内存缓存溢出到磁盘
        └── cache_001.png
        └── ...
```

**文件命名规则**:
```kotlin
fun generateFileName(mode: ScreenshotMode): String {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        .format(Date())
    return "${timestamp}_${mode.name}.png"
}
```

### 8.2 Room 数据库结构

**ScreenshotEntity**:
```kotlin
@Entity(tableName = "screenshots")
data class ScreenshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val filePath: String,
    val mode: String,           // "normal" or "long"
    val timestamp: Long,
    val pixelValue: Int?,       // 像素级截图的像素值
    val algorithm: String?,     // 拼接算法
    val resolution: String,     // 分辨率 "1920x3000"
    val fileSize: Long          // 文件大小 bytes
)
```

**ScreenshotDao**:
```kotlin
@Dao
interface ScreenshotDao {
    @Query("SELECT * FROM screenshots ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ScreenshotEntity>>
    
    @Query("SELECT * FROM screenshots WHERE mode = :mode")
    fun getByMode(mode: String): Flow<List<ScreenshotEntity>>
    
    @Insert
    suspend fun insert(screenshot: ScreenshotEntity)
    
    @Delete
    suspend fun delete(screenshot: ScreenshotEntity)
}
```

### 8.3 DataStore 配置存储

**SettingsDataStore**:
```kotlin
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsKeys {
    val DEFAULT_PIXEL = intPreferencesKey("default_pixel")
    val DEFAULT_ALGORITHM = stringPreferencesKey("default_algorithm")
    val QUALITY_LEVEL = stringPreferencesKey("quality_level")
    val STORAGE_PATH = stringPreferencesKey("storage_path")
    val FLOATING_WINDOW_ENABLED = booleanPreferencesKey("floating_window_enabled")
    val AUTO_CLEAN_DAYS = intPreferencesKey("auto_clean_days")
}
```

---

## 9. 性能优化策略

### 9.1 截图分辨率动态调整

```kotlin
fun calculateOptimalResolution(): Int {
    val devicePerformance = DevicePerformanceMonitor.getPerformanceLevel()
    
    return when (devicePerformance) {
        PerformanceLevel.HIGH -> 1080    // 高性能设备保持原始分辨率
        PerformanceLevel.MEDIUM -> 720   // 中等性能设备降低到 720p
        PerformanceLevel.LOW -> 480      // 低性能设备降低到 480p
    }
}
```

### 9.2 拼接异步处理

```kotlin
class ImageStitcher(private val scope: CoroutineScope) {
    private val stitchQueue = Channel<Bitmap>(capacity = 10)
    
    fun startStitching() {
        scope.launch(Dispatchers.Default) {
            for (segment in stitchQueue) {
                val stitchedResult = stitchSegment(segment)
                updateProgress(stitchedResult)
            }
        }
    }
    
    suspend fun addSegment(segment: Bitmap) {
        stitchQueue.send(segment)
    }
}
```

### 9.3 大图拼接的分块加载

```kotlin
class LargeBitmapLoader {
    fun loadBitmapRegion(bitmap: Bitmap, region: Rect): Bitmap {
        // 使用 BitmapRegionDecoder 分块加载
        val decoder = BitmapRegionDecoder.newInstance(bitmap.byteArray, false)
        return decoder.decodeRegion(region, BitmapFactory.Options())
    }
    
    fun displayLongBitmap(bitmap: Bitmap, scrollView: ScrollView) {
        // 只加载当前可见区域，滚动时动态加载
        val visibleRegion = calculateVisibleRegion(scrollView)
        val regionBitmap = loadBitmapRegion(bitmap, visibleRegion)
        display(regionBitmap)
    }
}
```

### 9.4 内存管理策略

```kotlin
class MemoryManager {
    private val maxCacheSize = 10 * 1024 * 1024 // 10MB
    
    fun manageMemory(currentCacheSize: Long) {
        if (currentCacheSize > maxCacheSize) {
            // 清理最早的缓存
            cleanOldestCache()
        }
        
        // 检查系统内存压力
        val systemMemory = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(systemMemory)
        
        if (systemMemory.lowMemory) {
            // 系统内存不足，强制清理所有缓存
            cleanAllCache()
        }
    }
}
```

---

## 10. 权限管理

### 10.1 MediaProjection 权限流程

```kotlin
class MediaProjectionManager(private val context: Context) {
    private var mediaProjection: MediaProjection? = null
    private var resultCode: Int = 0
    private var resultData: Intent? = null
    
    fun requestPermission(activity: Activity) {
        val manager = activity.getSystemService(MediaProjectionManager) as MediaProjectionManager
        val intent = manager.createScreenCaptureIntent()
        activity.startActivityForResult(intent, REQUEST_MEDIA_PROJECTION)
    }
    
    fun handlePermissionResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == REQUEST_MEDIA_PROJECTION && resultCode == RESULT_OK) {
            this.resultCode = resultCode
            this.resultData = data
            createMediaProjection()
        }
    }
    
    private fun createMediaProjection() {
        val manager = context.getSystemService(MediaProjectionManager) as MediaProjectionManager
        mediaProjection = manager.getMediaProjection(resultCode, resultData!!)
        
        // 注册回调，监听权限失效
        mediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                // 权限失效，通知 UI 层重新请求
                notifyPermissionLost()
            }
        }, null)
    }
}
```

### 10.2 AccessibilityService 权限引导

```kotlin
class AccessibilityPermissionHelper {
    fun checkAccessibilityEnabled(): Boolean {
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
    
    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
    
    fun showPermissionGuideDialog() {
        // 显示引导对话框，告诉用户如何开启无障碍服务
        AlertDialog.Builder(context)
            .setTitle("需要开启无障碍服务")
           setMessage("长图拼接功能需要无障碍服务来实现自动滚动。\n\n请前往设置开启「ScreenshotApp」无障碍服务。")
            .setPositiveButton("去设置") { _, _ ->
                openAccessibilitySettings()
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
```

### 10.3 悬浮窗权限

```kotlin
fun checkFloatingWindowPermission(): Boolean {
    return Settings.canDrawOverlays(context)
}

fun requestFloatingWindowPermission(activity: Activity) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${activity.packageName}")
    )
    activity.startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
}
```

---

## 11. 异常处理

### 11.1 截图失败重试机制

```kotlin
class ScreenshotService {
    private val maxRetryCount = 3
    
    suspend fun captureWithRetry(): Bitmap? {
        var retryCount = 0
        
        while (retryCount < maxRetryCount) {
            try {
                val bitmap = capture()
                if (bitmap != null) {
                    return bitmap
                }
            } catch (e: Exception) {
                Log.e(TAG, "Screenshot failed: ${e.message}")
            }
            
            retryCount++
            delay(100) // 等待 100ms 后重试
        }
        
        // 重试失败，通知 UI 层
        notifyCaptureFailed()
        return null
    }
}
```

### 11.2 拼接异常回滚

```kotlin
class ImageStitcher {
    private val recentSegments = mutableListOf<Bitmap>() // 保存最近 2 次截图
    
    fun stitchWithRollback(newSegment: Bitmap): StitchResult {
        val overlap = OverlapDetector.detectOverlap(recentSegments.last(), newSegment)
        
        if (overlap == -1) {
            // 检测失败，回滚最近 1 次截图
            recentSegments.removeLast()
            return StitchResult.Failure(needRetry = true)
        }
        
        val croppedSegment = newSegment.cropTop(overlap)
        recentSegments.add(croppedSegment)
        
        return StitchResult.Success(stitchAll(recentSegments))
    }
}
```

### 11.3 存储空间检测

```kotlin
fun checkStorageSpace(requiredSize: Long): Boolean {
    val stat = StatFs(storagePath)
    val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
    
    return availableBytes > requiredSize
}

fun notifyStorageInsufficient() {
    AlertDialog.Builder(context)
        .setTitle("存储空间不足")
        .setMessage("剩余空间不足以保存长图。\n\n请清理存储空间或降低截图质量。")
        .setPositiveButton("清理临时文件") { _, _ ->
            TempFileManager.cleanAllTempFiles()
        }
        .setNegativeButton("降低质量", null)
        .show()
}
```

---

## 12. 实施计划

### 12.1 开发阶段划分

**第一阶段：基础功能 (2-3 周)**
- 项目初始化和架构搭建
- MediaProjection 权限申请和管理
- 普通截图功能实现（整屏、区域、延时）
- Room 数据库和 DataStore 配置
- 主界面 UI 开发（Compose）

**第二阶段：长图拼接核心 (3-4 周)**
- AccessibilityService 实现和滚动控制
- 像素级滑动拼接算法实现
- 一屏一屏拼接模式实现
- 智能自动边界检测实现
- 悬浮窗 UI 开发

**第三阶段：算法优化 (2-3 周)**
- 重叠区域检测算法优化（模板匹配、特征点检测）
- 滑动距离补偿算法实现
- 防断裂和防重复机制完善
- 大图拼接性能优化
- 内存管理和缓存策略

**第四阶段：辅助功能 (1-2 周)**
- 截图编辑功能（裁剪、标注、马赛克）
- 历史记录管理界面
- 分享和导出功能
- 定期清理 WorkManager 任务

**第五阶段：测试和优化 (1-2 周)**
- 多设备兼容性测试
- 性能压力测试（超长图拼接）
- 内存泄漏检测（LeakCanary）
- 边缘情况测试（权限丢失、存储不足）
- UI 细节优化和用户体验改进

### 12.2 技术风险

**风险一：AccessibilityService 滚动精度**
- 部分应用的控件不支持精确像素滚动
- 解决方案：结合手势模拟作为补充方案，提供多种滚动方式

**风险二：图像拼接计算量**
- 模板匹配和特征点检测计算量较大，可能影响性能
- 解决方案：使用 OpenCV 库优化算法性能，或采用轻量级算法

**风险三：内存占用**
- 超长图拼接可能占用大量内存
- 解决方案：分段存储到磁盘，使用分块加载策略，设置最大拼接高度限制

**风险四：权限用户接受度**
- MediaProjection 权限需要用户每次授权
- AccessibilityService 权限引导可能被用户忽略
- 解决方案：提供清晰的权限说明和引导流程，简化授权步骤

---

## 13. 项目初始化配置

### 13.1 build.gradle 配置

```kotlin
android {
    compileSdk = 34
    targetSdk = 34
    minSdk = 21 // Android 5.0，MediaProjection API 最低要求
    
    defaultConfig {
        applicationId = "com.example.screenshotapp"
        versionCode = 1
        versionName = "1.0.0"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}

dependencies {
    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.0")
    implementation("androidx.activity:activity-compose:1.7.2")
    
    // Jetpack ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")
    
    // Jetpack Room
    implementation("androidx.room:room-runtime:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")
    ksp("androidx.room:room-compiler:2.5.2")
    
    // Jetpack DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Jetpack Navigation
    implementation("androidx.navigation:navigation-compose:2.7.0")
    
    // Jetpack WorkManager
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Coil
    implementation("io.coil-kt:coil-compose:2.4.0")
    
    // Accompanist (权限请求)
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    
    // OpenCV (可选，用于图像处理)
    implementation("org.opencv:opencv-android:4.8.0")
    
    // LeakCanary (调试)
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
}
```

### 13.2 AndroidManifest.xml 配置

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.screenshotapp">
    
    <!-- 权限声明 -->
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        android:maxSdkVersion="28" />
    
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.ScreenshotApp">
        
        <!-- AccessibilityService -->
        <service
            android:name=".service.ScrollAccessibilityService"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
            android:exported="false">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/accessibility_service_config" />
        </service>
        
        <!-- 前台服务（截图服务） -->
        <service
            android:name=".service.ScreenshotForegroundService"
            android:foregroundServiceType="mediaProjection"
            android:exported="false" />
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.ScreenshotApp">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

---

## 14. 总结

本设计方案详细定义了 Android 截图工具的技术架构、核心功能、算法实现和 UI 设计。采用 Kotlin + Jetpack Compose + MVVM 架构，使用 Android 14 (API 34) 作为目标平台，充分利用 Jetpack 组件体系构建现代化的 Android 应用。

核心亮点：
- **三种长图拼接模式**：满足不同场景需求
- **像素级完美拼接算法**：重叠检测 + 偏差补偿 + 防断裂防重复机制
- **灵活的参数配置**：档位 + 自定义结合
- **现代化的技术栈**：Compose + MVVM + Jetpack 组件
- **完善的性能优化**：异步处理、分块加载、内存管理

下一步：开始实施第一阶段的开发工作。