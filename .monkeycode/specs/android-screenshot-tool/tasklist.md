# Android 截图工具 - 任务清单

**项目名称**: Android LongScreenshot Tool  
**当前阶段**: 第一阶段 - 基础功能开发  
**预计时间**: 2-3 周

---

## 第一阶段：基础功能 (Phase 1)

### 1. 项目初始化和架构搭建

- [x] **1.1 创建 Android 项目**
  - 使用 Android Studio 或命令行工具创建新的 Android 项目
  - 配置 Kotlin + Jetpack Compose
  - 设置目标 SDK 34，最低 SDK 21
  - 初始化 Git 仓库（已存在）

- [x] **1.2 配置 build.gradle**
  - 添加所有必要的 Jetpack 组件依赖（Compose, ViewModel, Room, DataStore, Navigation, WorkManager）
  - 添加第三方库依赖（Coil, Accompanist, OpenCV）
  - 配置 Compose 编译器版本
  - 配置 KSP 用于 Room 编译

- [x] **1.3 配置 AndroidManifest.xml**
  - 添加必要的权限声明（SYSTEM_ALERT_WINDOW, FOREGROUND_SERVICE, WRITE_EXTERNAL_STORAGE）
  - 注册 AccessibilityService
  - 注册前台截图服务
  - 配置 MainActivity

- [x] **1.4 创建项目目录结构**
  - 按照设计文档创建完整的模块目录结构
  - ui/, viewmodel/, service/, repository/, data/, algorithm/, util/, model/

### 2. MediaProjection 权限管理

- [ ] **2.1 实现 MediaProjectionManager**
  - 创建 MediaProjectionManager 类
  - 实现 MediaProjection 权限请求流程
  - 实现权限结果处理
  - 实现 MediaProjection 创建和回调注册

- [ ] **2.2 实现权限请求 UI**
  - 创建权限请求引导界面（Compose）
  - 实现 MediaProjection 权限请求按钮
  - 显示权限状态指示

- [ ] **2.3 测试权限流程**
  - 测试权限请求成功场景
  - 测试权限拒绝场景
  - 测试权限失效后的重新请求

### 3. 普通截图功能

- [ ] **3.1 实现 ScreenshotService 核心**
  - 创建 ScreenshotService 类
  - 实现 VirtualDisplay 创建
  - 实现 ImageReader 配置
  - 实现单张截图捕获逻辑

- [ ] **3.2 实现整屏截图**
  - 实现整屏截图功能
  - 实现截图结果回调
  - 实现截图保存逻辑

- [ ] **3.3 实现区域截图**
  - 实现截图区域选择 UI
  - 实现区域截图捕获逻辑
  - 实现裁剪保存逻辑

- [ ] **3.4 实现延时截图**
  - 实现延时计时器（3s, 5s, 10s）
  - 实现延时截图触发逻辑
  - 实现延时倒计时 UI

- [ ] **3.5 测试截图功能**
  - 测试整屏截图功能
  - 测试区域截图功能
  - 测试延时截图功能
  - 测试截图质量

### 4. 数据存储配置

- [x] **4.1 配置 Room 数据库**
  - 创建 ScreenshotEntity 实体类
  - 创建 ScreenshotDao 数据访问对象
  - 创建 AppDatabase 数据库类
  - 实现数据库初始化逻辑

- [ ] **4.2 实现截图记录存储**
  - 实现截图记录插入逻辑
  - 实现截图记录查询逻辑
  - 实现截图记录删除逻辑
  - 测试数据库操作

- [x] **4.3 配置 DataStore**
  - 创建 SettingsDataStore 类
  - 定义设置键值（DEFAULT_PIXEL, DEFAULT_ALGORITHM, QUALITY_LEVEL, STORAGE_PATH, FLOATING_WINDOW_ENABLED, AUTO_CLEAN_DAYS）
  - 实现配置读写逻辑
  - 测试配置存储

- [ ] **4.4 实现文件存储管理**
  - 创建 FileStorageManager 类
  - 实现文件命名规则（yyyyMMdd_HHmmss_mode.png）
  - 实现文件保存逻辑
  - 实现目录结构创建（normal/, long/, temp/）

### 5. 主界面 UI 开发

- [ ] **5.1 实现底部导航**
  - 创建底部导航组件
  - 实现四个标签页（截图、长截图、历史、设置）
  - 实现导航切换逻辑

- [ ] **5.2 实现截图页 UI**
  - 创建截图页界面（ScreenshotScreen）
  - 实现整屏截图按钮
  - 实现区域截图按钮
  - 实现延时截图选择器
  - 实现截图状态显示

- [ ] **5.3 实现长截图页 UI**
  - 创建长截图页界面（LongScreenshotScreen）
  - 实现三种模式选择卡片
  - 实现参数设置面板
  - 实现开始按钮

- [ ] **5.4 实现历史记录页 UI**
  - 创建历史记录页界面（HistoryScreen）
  - 实现截图列表显示
  - 实现缩略图预览
  - 实现删除和分享按钮

- [ ] **5.5 实现设置页 UI**
  - 创建设置页界面（SettingsScreen）
  - 实现像素值档位选择
  - 实现拼接算法选择
  - 实现截图质量选择
  - 实现存储路径设置
  - 实现悬浮窗开关
  - 实现定期清理设置

- [ ] **5.6 实现截图完成后的编辑界面**
  - 创建编辑界面（EditScreen）
  - 实现裁剪功能
  - 实现标注功能（基础）
  - 实现分享菜单

- [ ] **5.7 测试 UI 交互**
  - 测试导航切换
  - 测试各页面功能
  - 测试设置保存和加载
  - 测试 UI 响应速度

### 6. ViewModel 层实现

- [ ] **6.1 实现 ScreenshotViewModel**
  - 创建 ScreenshotViewModel 类
  - 实现截图意图处理
  - 实现截图状态管理
  - 实现 UI 状态更新

- [ ] **6.2 实现 SettingsViewModel**
  - 创建 SettingsViewModel 类
  - 实现设置意图处理
  - 实现配置状态管理
  - 实现配置持久化

- [ ] **6.3 测试 ViewModel**
  - 测试状态管理逻辑
  - 测试意图处理流程
  - 测试与 Repository 的交互

---

## 第二阶段：长图拼接核心 (Phase 2)

（待第一阶段完成后细化）

### 7. AccessibilityService 实现
### 8. 滚动控制实现
### 9. 像素级拼接算法实现
### 10. 一屏拼接模式实现
### 11. 智能边界检测实现
### 12. 悬浮窗 UI 实现

---

## 第三阶段：算法优化 (Phase 3)

（待第二阶段完成后细化）

### 13. 重叠检测算法优化
### 14. 滑动补偿算法实现
### 15. 防断裂防重复机制完善
### 16. 性能优化实现

---

## 第四阶段：辅助功能 (Phase 4)

（待第三阶段完成后细化）

### 17. 截图编辑功能完善
### 18. 分享和导出功能
### 19. 定期清理任务

---

## 第五阶段：测试和优化 (Phase 5)

（待第四阶段完成后细化）

### 20. 兼容性测试
### 21. 性能测试
### 22. 用户体验优化

---

## 任务执行说明

- 每个任务完成后，将 `[ ]` 改为 `[x]`
- 每完成一个任务后，停止并等待用户确认
- 遵循 TDD 原则，先写测试再实现（如果适用）
- 所有测试必须通过后才能标记任务完成