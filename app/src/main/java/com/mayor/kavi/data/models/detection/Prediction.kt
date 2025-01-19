package com.mayor.kavi.data.models.detection

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Prediction(
    @SerialName("x")
    val x: Float,
    @SerialName("y")
    val y: Float,
    @SerialName("width")
    val width: Float,
    @SerialName("height")
    val height: Float,
    @SerialName("confidence")
    val confidence: Double,
    @SerialName("class")
    val `class`: String,
    @SerialName("class_id")
    val classId: Int,
    @SerialName("detection_id")
    val detectionId: String,
    @SerialName("image_path")
    val imagePath: String,
    @SerialName("prediction_type")
    val predictionType: String
)
