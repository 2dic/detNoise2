package com.example.noisedet2.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.noisedet2.core.data.local.entity.NoiseMeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoiseMeasurementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(measurement: NoiseMeasurementEntity): Long

    @Query("SELECT * FROM noise_measurements ORDER BY timestamp DESC")
    fun getAll(): Flow<List<NoiseMeasurementEntity>>

    @Query("SELECT * FROM noise_measurements ORDER BY timestamp DESC")
    suspend fun getAllOnce(): List<NoiseMeasurementEntity>

    @Query("SELECT * FROM noise_measurements WHERE id = :id")
    suspend fun getById(id: Long): NoiseMeasurementEntity?

    @Delete
    suspend fun delete(measurement: NoiseMeasurementEntity)

    @Query("DELETE FROM noise_measurements WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM noise_measurements")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM noise_measurements")
    suspend fun count(): Int

    @Query("SELECT AVG(dbLevel) FROM noise_measurements")
    suspend fun averageDb(): Float?

    @Query("SELECT MAX(dbLevel) FROM noise_measurements")
    suspend fun maxDb(): Float?

    @Query("SELECT MIN(dbLevel) FROM noise_measurements")
    suspend fun minDb(): Float?
}
