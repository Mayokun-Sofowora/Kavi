package com.mayor.kavi.data.models.detection

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Represents a request to detect dice in an image.
 * Supports multiple input types (ByteArray, File, or Bitmap) and converts them as needed.
 */
data class DetectionRequest(
    private val imageData: ByteArray? = null,
    private val imageFile: File? = null,
    private val imageBitmap: Bitmap? = null
) {
    fun getImageBytes(): ByteArray {
        return when {
            imageData != null -> imageData
            imageFile != null -> imageFile.readBytes()
            imageBitmap != null -> ByteArrayOutputStream().use { stream ->
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                stream.toByteArray()
            }
            else -> throw IllegalStateException("No image data provided")
        }
    }

    companion object {
        fun fromByteArray(bytes: ByteArray) = DetectionRequest(imageData = bytes)
        fun fromFile(file: File) = DetectionRequest(imageFile = file)
        fun fromBitmap(bitmap: Bitmap) = DetectionRequest(imageBitmap = bitmap)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DetectionRequest
        
        // Compare only the first element that's not null
        return when {
            imageData != null && other.imageData != null -> imageData.contentEquals(other.imageData)
            imageFile != null && other.imageFile != null -> imageFile == other.imageFile
            imageBitmap != null && other.imageBitmap != null -> imageBitmap == other.imageBitmap
            else -> false
        }
    }

    override fun hashCode(): Int {
        return when {
            imageData != null -> imageData.contentHashCode()
            imageFile != null -> imageFile.hashCode()
            imageBitmap != null -> imageBitmap.hashCode()
            else -> 0
        }
    }
} 