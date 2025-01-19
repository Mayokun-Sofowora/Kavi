package com.mayor.kavi.data.repository

import android.graphics.Bitmap
import com.mayor.kavi.data.models.detection.Detection

/**
 * Repository interface for handling dice detection operations using Roboflow API.
 */
interface RoboflowRepository {
    /**
     * Detects dice in the given bitmap image.
     * 
     * @param bitmap The image to analyze for dice detection
     * @return List of detected dice with their positions and confidence scores
     */
    suspend fun detectDice(bitmap: Bitmap): List<Detection>
} 