package com.example.noisedet2.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.noisedet2.core.data.local.dao.NoiseMeasurementDao
import com.example.noisedet2.core.data.local.entity.NoiseMeasurementEntity

@Database(
    entities = [NoiseMeasurementEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noiseMeasurementDao(): NoiseMeasurementDao
}
