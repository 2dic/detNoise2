package com.example.noisedet2.core.data.di

import android.content.Context
import androidx.room.Room
import com.example.noisedet2.core.data.local.AppDatabase
import com.example.noisedet2.core.data.local.dao.NoiseMeasurementDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "noise_det.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideNoiseMeasurementDao(database: AppDatabase): NoiseMeasurementDao {
        return database.noiseMeasurementDao()
    }
}
