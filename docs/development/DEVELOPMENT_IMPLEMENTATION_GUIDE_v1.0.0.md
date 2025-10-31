# DrowsyGuard (疲勞Bye) - 疲劳检测应用实现文档

## 项目概述

DrowsyGuard 是一个基于 MediaPipe 的 Android 疲劳检测应用，使用先进的计算机视觉技术实时监测用户的疲劳状态，并在检测到疲劳时提供警报。

## 核心功能实现

### 1️⃣ 图像输入与面部特征点检测

✅ **已实现功能：**
- 使用 MediaPipe FaceLandmarker 跟踪面部特征点
- 通过 CameraX 捕获前置摄像头画面
- 提取 EAR (Eye Aspect Ratio) 特征点
- 提取 MAR (Mouth Aspect Ratio) 特征点
- 获取面部方向数据

**实现位置：**
- `app/src/main/java/com/patrick/main/FaceLandmarkerHelper.kt`
- `app/src/main/java/com/patrick/main/fragment/CameraFragment.kt`

### 2️⃣ 核心疲劳检测逻辑

✅ **已实现功能：**

#### 👁 眼睛闭合检测
- 使用 EAR 检测眨眼和长时间眼睛闭合
- EAR 低于阈值 (0.25) 持续超过 1.5 秒 → 异常事件
- 跟踪每分钟眨眼频率，超过阈值 (20次/分钟) → 异常事件

#### 😮 打哈欠检测
- 检测嘴巴张开度或计算 MAR
- 嘴巴张开超过阈值 (MAR > 0.7) 持续超过 1 秒 → 异常事件

#### 📈 疲劳累积评估
- 每个检测到的异常事件增加计数器
- 当异常事件达到阈值 (3次) 时：
  - 触发声音警报
  - 显示视觉警告 (繁体中文)

**实现位置：**
- `detection-logic/src/main/java/com/patrick/detection/FatigueDetector.kt`

### 3️⃣ 用户警报与警告系统

✅ **已实现功能：**
- 播放警告声音 (warning.wav)
- 显示屏幕警告消息 (使用繁体中文)
- 使用 Canvas 覆盖层显示警告
- 震动提醒功能
- 不同疲劳级别的不同警报策略

**实现位置：**
- `user-alert/src/main/java/com/patrick/alert/FatigueAlertManager.kt`
- `app/src/main/java/com/patrick/main/FatigueOverlayView.kt`

### 4️⃣ 模块化清洁架构实现

✅ **已实现架构：**

#### 模块结构：
- **camera-input**: 封装 CameraX 和摄像头初始化
- **detection-logic**: 核心疲劳检测逻辑和状态管理
- **user-alert**: 处理声音和视觉警报
- **user-settings**: 用户配置管理 (预留)
- **shared-core**: 共享常量和资源
- **app-main**: 入口点和导航控制器

#### 架构特点：
- 遵循 Clean Architecture 原则
- 模块间松耦合
- 单一职责原则
- 依赖注入模式

### 5️⃣ 样式与资源管理

✅ **已实现功能：**
- 使用 `gradle/libs.versions.toml` 管理所有依赖版本
- 支持 Noto Sans CJK 字体 (繁体中文支持)
- 在 shared-core 模块中集中资源管理
- 统一的颜色和常量定义

**实现位置：**
- `gradle/libs.versions.toml`
- `shared-core/src/main/java/com/patrick/core/Constants.kt`

## 技术架构

### 核心组件

1. **FatigueDetector**: 疲劳检测核心算法
2. **FatigueAlertManager**: 警报管理
3. **FatigueDetectionManager**: 疲劳检测管理器
4. **FatigueOverlayView**: 疲劳状态显示覆盖层

### 数据流

```
摄像头输入 → FaceLandmarker → FatigueDetector → FatigueDetectionManager → UI更新 + 警报
```

### 模块依赖关系

```
app-main
├── detection-logic
├── user-alert
├── shared-core
├── camera-input
├── user-settings
└── account-auth

detection-logic
├── shared-core
└── mediapipe-tasks-vision

user-alert
├── detection-logic
└── shared-core
```

## 配置参数

### 疲劳检测参数
- **EAR 阈值**: 0.25 (可配置范围: 0.15-0.35)
- **MAR 阈值**: 0.7 (可配置范围: 0.5-0.9)
- **眼睛闭合持续时间阈值**: 1500ms
- **打哈欠持续时间阈值**: 1000ms
- **眨眼频率阈值**: 20次/分钟
- **疲劳事件累积阈值**: 3次

### 警报配置
- **警报显示持续时间**: 3000ms
- **声音警报持续时间**: 2000ms
- **震动持续时间**: 500ms

## 使用说明

### 启动应用
1. 打开应用，授予摄像头权限
2. 进入摄像头界面
3. 疲劳检测自动启动

### 疲劳检测
- **正常状态**: 绿色指示器
- **中度疲劳**: 橙色警告，声音+震动提醒
- **严重疲劳**: 红色紧急警告，循环声音+强烈震动

### 事件指示器
- 👁 眼睛闭合事件
- 😮 打哈欠事件  
- 👀 高频眨眼事件

## 扩展功能

### 未来计划
- [ ] 用户账户集成
- [ ] 本地数据库存储疲劳事件历史
- [ ] 个性化设置页面
- [ ] 疲劳趋势分析
- [ ] 云端数据同步

## 技术栈

- **语言**: Kotlin
- **架构**: Clean Architecture + MVVM
- **UI**: Android Views + Canvas
- **计算机视觉**: MediaPipe
- **摄像头**: CameraX
- **依赖管理**: Version Catalogs (libs.versions.toml)

## 性能优化

- 使用后台线程处理疲劳检测
- 优化图像处理管道
- 内存管理优化
- 电池使用优化

## 安全与隐私

- 所有处理在本地进行
- 不收集或传输个人数据
- 摄像头权限最小化使用
- 符合 GDPR 和隐私法规

## 构建说明

1. 确保 Android Studio 版本支持
2. 同步 Gradle 依赖
3. 添加音频资源文件到 `app/src/main/res/raw/`
4. 构建并运行应用

## 故障排除

### 常见问题
1. **摄像头权限**: 确保授予摄像头权限
2. **音频文件**: 确保 warning.wav 和 emergency.wav 存在
3. **MediaPipe 模型**: 确保 face_landmarker.task 文件存在
4. **内存问题**: 在低端设备上可能需要调整检测频率

### 调试模式
启用详细日志记录以诊断问题：
```kotlin
Log.d("FatigueDetector", "检测到疲劳事件")
```

---

**版本**: 1.0.0  
**最后更新**: 2024年  
**开发者**: Patrick  
**许可证**: Apache 2.0 