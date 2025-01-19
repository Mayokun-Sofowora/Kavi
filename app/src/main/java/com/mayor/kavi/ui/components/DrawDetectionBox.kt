package com.mayor.kavi.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.mayor.kavi.data.models.detection.Detection

private val diceColors = mapOf(
    "1" to Color(0xFF1E88E5), // Blue
    "2" to Color(0xFF43A047), // Green
    "3" to Color(0xFFE53935), // Red
    "4" to Color(0xFFFFB300), // Amber
    "5" to Color(0xFF8E24AA), // Purple
    "6" to Color(0xFFFF6D00)  // Orange
)

@Composable
fun DrawDetectionBox(
    detection: Detection,
    imageSize: Size,
    canvasSize: Size
) {
    val scaleX = canvasSize.width / 640f
    val scaleY = canvasSize.height / 640f
    
    val boxColor = diceColors[detection.label] ?: Color.Green

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw bounding box with scaled coordinates
            drawRect(
                color = boxColor,
                topLeft = Offset(
                    detection.boundingBox.left * scaleX,
                    detection.boundingBox.top * scaleY
                ),
                size = Size(
                    detection.boundingBox.width() * scaleX,
                    detection.boundingBox.height() * scaleY
                ),
                style = Stroke(width = 2f)
            )

            // Draw label directly on canvas
            val labelText = "${detection.label} (${(detection.confidence * 100).toInt()}%)"
            val labelX = detection.boundingBox.left * scaleX
            val labelY = if (detection.boundingBox.top * scaleY > 30) {
                detection.boundingBox.top * scaleY - 10
            } else {
                detection.boundingBox.bottom * scaleY + 15
            }

            drawContext.canvas.nativeCanvas.drawText(
                labelText,
                labelX,
                labelY,
                android.graphics.Paint().apply {
                    color = boxColor.toArgb()
                    textSize = 35f
                    isFakeBoldText = true
                }
            )
        }
    }
}
