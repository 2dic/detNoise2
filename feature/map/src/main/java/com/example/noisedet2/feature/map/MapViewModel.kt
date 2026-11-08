package com.example.noisedet2.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noisedet2.core.common.noise.NoiseMeter
import com.example.noisedet2.core.data.map.MapFocus
import com.example.noisedet2.core.data.repository.NoiseRepository
import com.example.noisedet2.core.ui.noise.noiseCategoryLabel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: NoiseRepository,
    private val mapFocus: MapFocus,
) : ViewModel() {

    private val noiseMeter = NoiseMeter()
    private var measurementJob: Job? = null
    private val controls = MutableStateFlow(Controls())
    private var centeredOnUser = false

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    val uiState: StateFlow<MapUiState> = combine(
        repository.observeAll(),
        controls
    ) { measurements, controls ->
        MapUiState(
            measurements = measurements,
            liveDb = controls.liveDb,
            status = controls.status,
            isMeasuring = controls.isMeasuring,
            selected = measurements.firstOrNull { it.id == controls.selectedId },
            userLatitude = controls.userLatitude,
            userLongitude = controls.userLongitude,
            cameraToken = controls.cameraToken,
            cameraLatitude = controls.cameraLatitude,
            cameraLongitude = controls.cameraLongitude,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MapUiState()
    )

    init {
        mapFocus.target.value?.let(::applyFocus)
        viewModelScope.launch {
            mapFocus.target.collect { target ->
                if (target != null) applyFocus(target)
            }
        }
    }

    /**
     * Centers the map on the user only once, and only when the screen was not
     * opened to show a specific measurement.
     */
    fun shouldRequestInitialLocation(): Boolean {
        if (centeredOnUser) return false
        centeredOnUser = true
        return controls.value.selectedId == null
    }

    fun onUserLocation(latitude: Double, longitude: Double, moveCamera: Boolean) {
        controls.update { current ->
            current.copy(
                userLatitude = latitude,
                userLongitude = longitude,
                cameraToken = if (moveCamera) System.nanoTime() else current.cameraToken,
                cameraLatitude = if (moveCamera) latitude else current.cameraLatitude,
                cameraLongitude = if (moveCamera) longitude else current.cameraLongitude,
            )
        }
    }

    fun onLocationUnavailable() {
        _messages.tryEmit("Location is unavailable")
    }

    fun hasSelection(): Boolean = controls.value.selectedId != null

    fun selectMeasurement(id: Long) {
        controls.update { it.copy(selectedId = id) }
    }

    private fun applyFocus(target: MapFocus.Target) {
        controls.update {
            it.copy(
                selectedId = target.measurementId,
                cameraToken = target.token,
                cameraLatitude = target.latitude,
                cameraLongitude = target.longitude,
            )
        }
        mapFocus.consume(target.token)
    }

    fun clearSelection() {
        controls.update { it.copy(selectedId = null) }
    }

    fun startMeasurement() {
        if (controls.value.isMeasuring) return
        val samples = mutableListOf<Float>()
        controls.update { it.copy(isMeasuring = true, status = "Measuring…") }

        measurementJob = viewModelScope.launch {
            var finished = false
            fun finishOnce() {
                if (finished) return
                finished = true
                finishMeasurement(samples)
            }
            try {
                withTimeout(MEASUREMENT_DURATION_MS) {
                    noiseMeter.measure().collect { db ->
                        samples.add(db)
                        publishLive(db)
                    }
                }
                finishOnce()
            } catch (_: kotlinx.coroutines.TimeoutCancellationException) {
                finishOnce()
            } catch (e: CancellationException) {
                finishOnce()
                throw e
            } catch (e: Exception) {
                controls.update {
                    it.copy(
                        isMeasuring = false,
                        status = "Error: ${e.message ?: "unknown"}"
                    )
                }
                _messages.tryEmit("Measurement failed")
            }
        }
    }

    fun stopMeasurement() {
        measurementJob?.cancel()
        measurementJob = null
    }

    private fun publishLive(db: Float) {
        controls.update {
            it.copy(
                liveDb = db,
                status = noiseCategoryLabel(NoiseMeter.levelCategory(db))
            )
        }
    }

    private fun finishMeasurement(samples: List<Float>) {
        val average = samples.takeIf { it.isNotEmpty() }?.average()?.toFloat()
        if (average == null) {
            controls.update { it.copy(isMeasuring = false, status = "Ready to measure") }
            return
        }
        controls.update {
            it.copy(
                isMeasuring = false,
                liveDb = average,
                status = "Result: ${noiseCategoryLabel(NoiseMeter.levelCategory(average))}"
            )
        }
        saveMeasurement(average)
    }

    private fun saveMeasurement(dbLevel: Float) {
        val latitude = controls.value.userLatitude
        val longitude = controls.value.userLongitude
        if (latitude == null || longitude == null) {
            _messages.tryEmit("Measurement not saved — location unknown")
            return
        }
        viewModelScope.launch {
            try {
                repository.save(
                    latitude = latitude,
                    longitude = longitude,
                    dbLevel = dbLevel
                )
                _messages.tryEmit(
                    "Saved: ${String.format(Locale.US, "%.1f", dbLevel)} dB"
                )
            } catch (_: Exception) {
                _messages.tryEmit("Failed to save measurement")
            }
        }
    }

    private data class Controls(
        val liveDb: Float? = null,
        val status: String = "Ready to measure",
        val isMeasuring: Boolean = false,
        val selectedId: Long? = null,
        val userLatitude: Double? = null,
        val userLongitude: Double? = null,
        val cameraToken: Long = 0L,
        val cameraLatitude: Double? = null,
        val cameraLongitude: Double? = null,
    )

    private companion object {
        const val MEASUREMENT_DURATION_MS = 4_000L
    }
}
