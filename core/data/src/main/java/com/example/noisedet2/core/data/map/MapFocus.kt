package com.example.noisedet2.core.data.map

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * One-shot request to open a saved measurement on the map.
 * History writes it; the map screen applies it and clears it.
 */
@Singleton
class MapFocus @Inject constructor() {

    private val _target = MutableStateFlow<Target?>(null)
    val target: StateFlow<Target?> = _target.asStateFlow()

    fun request(measurementId: Long, latitude: Double, longitude: Double) {
        _target.value = Target(
            measurementId = measurementId,
            latitude = latitude,
            longitude = longitude,
            token = System.nanoTime(),
        )
    }

    fun consume(token: Long) {
        _target.update { current ->
            if (current?.token == token) null else current
        }
    }

    data class Target(
        val measurementId: Long,
        val latitude: Double,
        val longitude: Double,
        val token: Long,
    )
}
