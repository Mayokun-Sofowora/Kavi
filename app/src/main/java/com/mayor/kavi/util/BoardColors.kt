package com.mayor.kavi.util

import androidx.compose.ui.graphics.*

object BoardColors {
    // Solid colors
    private val colorMap = mapOf(
        "default" to Color(0xFFD3D3D3),  // Dark gray
        "blue" to Color(0xFF90CAF9),     // Material Blue 200
        "green" to Color(0xFFA5D6A7),    // Material Green 200
        "red" to Color(0xFFEF9A9A),      // Material Red 200
        "orange" to Color(0xFFFFCC80),   // Material Orange 200
        "night" to Color(0xFF444444)     // Dark gray
    )

    // Color blend/mixture definitions
    private val blendedColors = mapOf(
        "sea-foam" to Color(0xFF98FF98),      // Blend of green and white
        "coral" to Color(0xFFFF7F50),        // Blend of orange and pink
        "lavender" to Color(0xFFE6E6FA),     // Blend of light blue and pink
        "turquoise" to Color(0xFF40E0D0)     // Blend of blue and green
    )

    // Gradient definitions
    private val gradientMap = mapOf(
        "sunset" to Brush.horizontalGradient(listOf(Color(0xFFFF8C00), Color(0xFFFF0080))),
        "ocean" to Brush.verticalGradient(listOf(Color(0xFF00B4DB), Color(0xFF0083B0))),
        "forest" to Brush.verticalGradient(listOf(Color(0xFF56AB2F), Color(0xFFA8E063))),
        "twilight" to Brush.linearGradient(listOf(Color(0xFF2C3E50), Color(0xFF3498DB))),
        "fire" to Brush.horizontalGradient(listOf(Color(0xFFFF416C), Color(0xFFFF4B2B))),
        "beach" to Brush.horizontalGradient(listOf(Color(0xFFFCEEB5), Color(0xFF4BC0C8))),
        "sunrise" to Brush.verticalGradient(listOf(Color(0xFFFF5722), Color(0xFFFFEB3B)))
    )

    fun getColor(colorName: String): Any {
        // First check if it's a gradient
        gradientMap[colorName]?.let { return it }

        // Then check if it's a blended color
        blendedColors[colorName]?.let { return it }

        // Finally check solid colors, with default fallback
        return colorMap[colorName] ?: colorMap["default"]!!
    }

    // Helper method to get available color names for settings
    fun getAvailableColors(): List<String> =
        (colorMap.keys + blendedColors.keys + gradientMap.keys).distinct()
}
