package com.patrick.core

/**
 * 应用常量定义
 */
object Constants {
    // 应用信息
    const val APP_NAME = "疲勞Bye"
    const val APP_VERSION = "1.0.0"

    // 疲劳检测相关常量
    object FatigueDetection {
        // EAR (Eye Aspect Ratio) 默认阈值 - 根據實際 EAR 值調整
        const val DEFAULT_EAR_THRESHOLD = 0.15f // 修正：睜眼約 0.13-0.15，閉眼約 0.07-0.10
        const val MIN_EAR_THRESHOLD = 0.15f
        const val MAX_EAR_THRESHOLD = 0.35f

        // MAR (Mouth Aspect Ratio) 默认阈值
        const val DEFAULT_MAR_THRESHOLD = 0.7f
        const val MIN_MAR_THRESHOLD = 0.5f
        const val MAX_MAR_THRESHOLD = 0.9f

        // 时间阈值 (毫秒)
        const val EYE_CLOSURE_DURATION_THRESHOLD = 2000L // 調整為 2 秒，減少誤判
        const val YAWN_DURATION_THRESHOLD = 1500L // 調整為 1.5 秒，減少誤判
        const val BLINK_INTERVAL_MIN = 200L // 眨眼最小间隔

        // 眨眼频率阈值 (每分钟)
        const val BLINK_FREQUENCY_THRESHOLD = 25 // 調整為 25 次/分鐘，減少誤判

        // 疲劳事件累积阈值
        const val FATIGUE_EVENT_THRESHOLD = 2 // 調整為 2，減少誤判
        const val SEVERE_FATIGUE_MULTIPLIER = 2
    }

    // 警报相关常量
    object Alert {
        const val ALERT_DURATION_MS = 3000L
        const val SOUND_ALERT_DURATION_MS = 2000L
        const val VIBRATION_DURATION_MS = 500L
        const val EMERGENCY_ALERT_DURATION_MS = 5000L
    }

    // 摄像头相关常量
    object Camera {
        const val DEFAULT_CAMERA_FACING = "front"
        const val CAMERA_ASPECT_RATIO = "4:3"
        const val CAMERA_PREVIEW_WIDTH = 640
        const val CAMERA_PREVIEW_HEIGHT = 480
    }

    // 文件相关常量
    object Files {
        const val WARNING_SOUND_FILE = "warning.wav"
        const val EMERGENCY_SOUND_FILE = "emergency.wav"
        const val FACE_LANDMARKER_MODEL = "face_landmarker.task"
    }

    // 模型檔案路徑
    const val FACE_LANDMARKER_MODEL_PATH = "face_landmarker.task"

    // 字体相关常量
    object Fonts {
        const val NOTO_SANS_CJK = "fonts/NotoSansCJK-Regular.ttc"
        const val NOTO_SANS_CJK_BOLD = "fonts/NotoSansCJK-Bold.ttc"
        const val NOTO_SANS_CJK_TC = "fonts/NotoSansCJKtc-Regular.otf"
    }

    // 颜色常量
    object Colors {
        const val FATIGUE_NORMAL = "#00FF00" // 绿色 - 正常
        const val FATIGUE_MODERATE = "#FFA500" // 橙色 - 中度疲劳
        const val FATIGUE_SEVERE = "#FF0000" // 红色 - 严重疲劳
        const val WARNING_BACKGROUND = "#FFFF00" // 黄色 - 警告背景
    }

    // 本地化字符串键
    object StringKeys {
        const val FATIGUE_DETECTED = "fatigue_detected"
        const val MODERATE_FATIGUE_WARNING = "moderate_fatigue_warning"
        const val SEVERE_FATIGUE_WARNING = "severe_fatigue_warning"
        const val EYE_CLOSURE_DETECTED = "eye_closure_detected"
        const val YAWN_DETECTED = "yawn_detected"
        const val HIGH_BLINK_FREQUENCY = "high_blink_frequency"
        const val PLEASE_REST = "please_rest"
        const val STOP_DRIVING = "stop_driving"
    }

    // 偏好设置键
    object Preferences {
        const val PREF_NAME = "drowsyguard_preferences"
        const val KEY_EAR_THRESHOLD = "ear_threshold"
        const val KEY_MAR_THRESHOLD = "mar_threshold"
        const val KEY_FATIGUE_EVENT_THRESHOLD = "fatigue_event_threshold"
        const val KEY_SOUND_ENABLED = "sound_enabled"
        const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        const val KEY_ALERT_ENABLED = "alert_enabled"
    }

    // 数据库相关常量
    object Database {
        const val DATABASE_NAME = "drowsyguard.db"
        const val DATABASE_VERSION = 1

        // 表名
        const val TABLE_FATIGUE_EVENTS = "fatigue_events"
        const val TABLE_USER_SETTINGS = "user_settings"

        // 列名
        const val COLUMN_ID = "id"
        const val COLUMN_TIMESTAMP = "timestamp"
        const val COLUMN_EVENT_TYPE = "event_type"
        const val COLUMN_FATIGUE_LEVEL = "fatigue_level"
        const val COLUMN_DURATION = "duration"
        const val COLUMN_DESCRIPTION = "description"
    }

    // 权限相关常量
    object Permissions {
        const val CAMERA_PERMISSION = "android.permission.CAMERA"
        const val VIBRATE_PERMISSION = "android.permission.VIBRATE"
        const val WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE"
        const val READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE"
    }

    // 错误码
    object ErrorCodes {
        const val CAMERA_INITIALIZATION_FAILED = 1001
        const val FACE_DETECTION_FAILED = 1002
        const val FATIGUE_DETECTION_FAILED = 1003
        const val ALERT_SYSTEM_FAILED = 1004
        const val PERMISSION_DENIED = 1005
        const val MODEL_LOADING_FAILED = 1006
    }
}
