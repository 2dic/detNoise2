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
    private val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT
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
        return AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        ).also {
            require(it.state == AudioRecord.STATE_INITIALIZED) {
                "AudioRecord failed to initialize. Check RECORD_AUDIO permission."
            }
        }
    }

    private fun calculateDb(buffer: ShortArray, readCount: Int): Float {
        var sumSquares = 0.0
        for (i in 0 until readCount) {
            val sample = buffer[i].toDouble()
            sumSquares += sample * sample
        }

        val rms = sqrt(sumSquares / readCount)
        if (rms < 1.0) return 0f // silence floor

        // Convert RMS to dB. Reference: 32767 = full scale for 16-bit PCM.
        // Adding offset (~90–100) approximates real-world dB SPL on many devices.
        // This is relative and should be calibrated for absolute accuracy.
        val db = (20 * log10(rms)).toFloat() + 90f
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
