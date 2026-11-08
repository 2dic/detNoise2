package com.example.noisedet2.core.ui.noise

import androidx.compose.ui.graphics.Color
import com.example.noisedet2.core.common.noise.NoiseLevel
import java.text.DateFormat
import java.util.Date
import java.util.Locale

/**
 * Shared presentation helpers for noise values.
 * Used by every feature module so colors and labels stay consistent.
 */

fun noiseCategoryLabel(level: NoiseLevel): String = when (level) {
    NoiseLevel.QUIET -> "Quiet"
    NoiseLevel.MODERATE -> "Moderate"
    NoiseLevel.LOUD -> "Loud"
    NoiseLevel.VERY_LOUD -> "Very loud"
}

/** Marker / accent fill. Matches the project scale: green / yellow / orange / red. */
fun noiseMarkerColor(level: NoiseLevel): Int = when (level) {
    NoiseLevel.QUIET -> 0xFF4CAF50.toInt()
    NoiseLevel.MODERATE -> 0xFFFFEB3B.toInt()
    NoiseLevel.LOUD -> 0xFFFF9800.toInt()
    NoiseLevel.VERY_LOUD -> 0xFFF44336.toInt()
}

/** Text color for the value itself. Yellow is darkened so it stays readable on a light card. */
fun noiseTextColor(level: NoiseLevel): Color = when (level) {
    NoiseLevel.MODERATE -> Color(0xFFF9A825)
    else -> Color(noiseMarkerColor(level))
}

/** Content color for a filled chip/background using [noiseMarkerColor]. */
fun noiseOnColor(level: NoiseLevel): Color = when (level) {
    NoiseLevel.MODERATE -> Color(0xFF212121)
    else -> Color.White
}

fun formatDb(db: Float): String = String.format(Locale.US, "%.1f dB", db)

fun formatTimestamp(timestamp: Long): String {
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
        .format(Date(timestamp))
}

fun formatCoordinates(latitude: Double, longitude: Double): String {
    return String.format(Locale.US, "%.5f, %.5f", latitude, longitude)
}
