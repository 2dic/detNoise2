package com.example.noisedet2.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local representation of a single noise measurement.
 * Stored fully on-device (no cloud yet).
 */
@Entity(tableName = "noise_measurements")
data class NoiseMeasurementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val latitude: Double,
    val longitude: Double,

    /** Average noise level in dB */
    val dbLevel: Float,

    /** Unix timestamp in milliseconds */
    val timestamp: Long,

    /** Optional user note */
    val note: String? = null
)
