package com.mayor.kavi.data.models.detection

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DetectionResponse(
    @SerialName("predictions")
    val predictions: List<Prediction> = emptyList(),
    @SerialName("image")
    val image: ImageInfo? = null
)
