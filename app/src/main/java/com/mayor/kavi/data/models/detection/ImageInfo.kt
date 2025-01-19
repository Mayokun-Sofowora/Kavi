package com.mayor.kavi.data.models.detection

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImageInfo(
    @SerialName("width")
    val width: Int,
    @SerialName("height")
    val height: Int
)