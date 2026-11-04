package com.example.noisedet2.core.data.repository

import com.example.noisedet2.core.data.local.dao.NoiseMeasurementDao
import com.example.noisedet2.core.data.local.entity.NoiseMeasurementEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoiseRepository @Inject constructor(
    private val dao: NoiseMeasurementDao
) {

    fun observeAll(): Flow<List<NoiseMeasurementEntity>> = dao.getAll()

    suspend fun getAllOnce(): List<NoiseMeasurementEntity> = dao.getAllOnce()

    suspend fun getById(id: Long): NoiseMeasurementEntity? = dao.getById(id)

    /**
     * Saves a new measurement and returns the generated id.
     */
    suspend fun save(
        latitude: Double,
        longitude: Double,
        dbLevel: Float,
        timestamp: Long = System.currentTimeMillis(),
        note: String? = null
    ): Long {
        val entity = NoiseMeasurementEntity(
            latitude = latitude,
            longitude = longitude,
            dbLevel = dbLevel,
            timestamp = timestamp,
            note = note
        )
        return dao.insert(entity)
    }

    suspend fun delete(measurement: NoiseMeasurementEntity) = dao.delete(measurement)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun deleteAll() = dao.deleteAll()

    suspend fun count(): Int = dao.count()

    suspend fun averageDb(): Float? = dao.averageDb()

    suspend fun maxDb(): Float? = dao.maxDb()

    suspend fun minDb(): Float? = dao.minDb()
}
