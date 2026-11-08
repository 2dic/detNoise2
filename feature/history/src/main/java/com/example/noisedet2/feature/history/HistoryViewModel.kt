package com.example.noisedet2.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noisedet2.core.data.local.entity.NoiseMeasurementEntity
import com.example.noisedet2.core.data.map.MapFocus
import com.example.noisedet2.core.data.repository.NoiseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: NoiseRepository,
    private val mapFocus: MapFocus,
) : ViewModel() {

    val measurements: StateFlow<List<NoiseMeasurementEntity>> = repository.observeAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun showOnMap(measurement: NoiseMeasurementEntity) {
        mapFocus.request(
            measurementId = measurement.id,
            latitude = measurement.latitude,
            longitude = measurement.longitude,
        )
    }

    fun delete(measurement: NoiseMeasurementEntity) {
        viewModelScope.launch {
            runCatching { repository.delete(measurement) }
        }
    }

    fun restore(measurement: NoiseMeasurementEntity) {
        viewModelScope.launch {
            runCatching {
                repository.save(
                    latitude = measurement.latitude,
                    longitude = measurement.longitude,
                    dbLevel = measurement.dbLevel,
                    timestamp = measurement.timestamp,
                    note = measurement.note
                )
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            runCatching { repository.deleteAll() }
        }
    }
}
