package com.mayor.kavi.data.models.detection

import android.graphics.RectF

/**
 * Represents a detected dice in the image.
 *
 * @property boundingBox The rectangle boundary that encompasses the detected dice
 * @property label The detected dice value (e.g. "1", "2", etc.)
 * @property confidence A value between 0 and 1 representing the model's confidence
 */
data class Detection(
    val boundingBox: RectF,
    val label: String,
    val confidence: Float
)
