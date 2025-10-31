package com.patrick.detection

enum class DetectionState {
    INITIALIZING,
    CALIBRATING,
    DETECTING,
    WARNING,
    NOTICE,
    NO_FACE,
    REST_MODE,
    ERROR,
    SHUTDOWN
}