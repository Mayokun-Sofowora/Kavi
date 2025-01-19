package com.mayor.kavi.ui.viewmodel

import com.mayor.kavi.data.models.detection.Detection

sealed class DetectionState {
    data object Idle : DetectionState()
    data object Processing : DetectionState()
    data object NoDetections : DetectionState()
    data class Success(val detections: List<Detection>) : DetectionState()
    data class Error(val message: String) : DetectionState()
}