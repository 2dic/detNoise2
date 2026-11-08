package com.example.noisedet2.core.common.noise

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * Measures ambient noise level using the device microphone.
 *
 * Returns values in decibels (dB).
 * Note: Absolute accuracy on consumer devices is limited without calibration.
 * Values are useful for relative comparison.
 */
class NoiseMeter(
    private val sampleRate: Int = 44100,
    private val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    private val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT,
    /**
     * Offset added to dBFS to approximate dB SPL.
     * Consumer microphones vary; adjust this value if readings are consistently off.
     */
    private val calibrationOffsetDb: Float = 90f
) {

    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        .coerceAtLeast(sampleRate / 10) // at least ~100ms of audio

    /**
     * Continuously emits noise level in dB while the Flow is collected.
     * Call [measureAverage] for a single averaged measurement over a duration.
     */
    fun measure(): Flow<Float> = flow {
        val audioRecord = createAudioRecord()
        try {
            audioRecord.startRecording()
            val buffer = ShortArray(bufferSize)

            while (currentCoroutineContext().isActive) {
                val read = audioRecord.read(buffer, 0, buffer.size)
                if (read > 0) {
                    emit(calculateDb(buffer, read))
                }
            }
        } finally {
            runCatching {
                audioRecord.stop()
                audioRecord.release()
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Measures noise for [durationMs] milliseconds and returns the average dB value.
     */
    suspend fun measureAverage(durationMs: Long = 3000L): Float {
        val samples = mutableListOf<Float>()
        val start = System.currentTimeMillis()

        measure().collect { db ->
            samples.add(db)
            if (System.currentTimeMillis() - start >= durationMs) {
                return@collect
            }
            currentCoroutineContext().ensureActive()
        }

        return if (samples.isEmpty()) 0f else samples.average().toFloat()
    }

    private fun createAudioRecord(): AudioRecord {
        // UNPROCESSED avoids AGC / noise suppression when the device supports it.
        // MIC is the fallback for devices that reject the raw source.
        val sources = intArrayOf(
            MediaRecorder.AudioSource.UNPROCESSED,
            MediaRecorder.AudioSource.MIC
        )
        sources.forEach { source ->
            val record = AudioRecord(
                source,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )
            if (record.state == AudioRecord.STATE_INITIALIZED) {
                return record
            }
            record.release()
        }
        error("AudioRecord failed to initialize. Check RECORD_AUDIO permission.")
    }

    private fun calculateDb(buffer: ShortArray, readCount: Int): Float {
        var sumSquares = 0.0
        for (i in 0 until readCount) {
            val sample = buffer[i].toDouble()
            sumSquares += sample * sample
        }

        val rms = sqrt(sumSquares / readCount)
        if (rms < 1.0) return 0f // silence floor

        // 16-bit PCM full scale is 32768, so this is dBFS (always <= 0).
        // Adding the calibration offset converts it to an approximate dB SPL value.
        val dbfs = 20 * log10(rms / 32768.0)
        val db = dbfs.toFloat() + calibrationOffsetDb
        return db.coerceIn(0f, 140f)
    }

    companion object {
        /** Rough interpretation of dB levels for UI coloring */
        fun levelCategory(db: Float): NoiseLevel {
            return when {
                db < 50f -> NoiseLevel.QUIET
                db < 70f -> NoiseLevel.MODERATE
                db < 85f -> NoiseLevel.LOUD
                else -> NoiseLevel.VERY_LOUD
            }
        }
    }
}

enum class NoiseLevel {
    QUIET,      // < 50 dB  — green
    MODERATE,   // 50–70   — yellow
    LOUD,       // 70–85   — orange
    VERY_LOUD   // > 85     — red
}
