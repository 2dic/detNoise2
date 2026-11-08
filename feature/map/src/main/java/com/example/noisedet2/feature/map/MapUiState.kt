package com.example.noisedet2.feature.map

import com.example.noisedet2.core.data.local.entity.NoiseMeasurementEntity

data class MapUiState(
    val measurements: List<NoiseMeasurementEntity> = emptyList(),
    val liveDb: Float? = null,
    val status: String = "Ready to measure",
    val isMeasuring: Boolean = false,
    val selected: NoiseMeasurementEntity? = null,
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,
    val cameraToken: Long = 0L,
    val cameraLatitude: Double? = null,
    val cameraLongitude: Double? = null,
)
