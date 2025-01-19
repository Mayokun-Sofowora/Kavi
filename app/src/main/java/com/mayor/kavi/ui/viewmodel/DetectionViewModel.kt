package com.mayor.kavi.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayor.kavi.data.repository.RoboflowRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DetectionViewModel @Inject constructor(
    private val roboflowRepository: RoboflowRepository
) : ViewModel() {

    private val _detectionState = MutableStateFlow<DetectionState>(DetectionState.Idle)
    val detectionState: StateFlow<DetectionState> = _detectionState.asStateFlow()

    fun detectDice(bitmap: Bitmap) {
        _detectionState.value = DetectionState.Processing
        viewModelScope.launch {
            try {
                val detections = roboflowRepository.detectDice(bitmap)
                _detectionState.value = if (detections.isNotEmpty()) {
                    DetectionState.Success(detections)
                } else {
                    DetectionState.NoDetections
                }
            } catch (e: Exception) {
                Timber.e(e, "Error detecting dice")
                _detectionState.value = DetectionState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun clearDetections() {
        _detectionState.value = DetectionState.Idle
    }
}
