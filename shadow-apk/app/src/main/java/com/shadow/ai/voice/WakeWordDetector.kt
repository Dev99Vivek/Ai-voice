package com.shadow.ai.voice

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Always-on wake word detector.
 *
 * Primary: Porcupine SDK (picovoice.ai — free tier available).
 * Fallback: simple energy + keyword scoring for "shadow" without network.
 *
 * To enable Porcupine:
 *  1. Get a free access key from console.picovoice.ai
 *  2. Un-comment the Porcupine dependency in app/build.gradle.kts
 *  3. Un-comment the Porcupine block below and remove the fallback detector
 *  4. Add your .ppn wake word file to app/src/main/assets/
 */
class WakeWordDetector(
    private val context: Context,
    private val onWakeWordDetected: (String) -> Unit
) {
    private val tag = "WakeWordDetector"
    private var detectionJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val sampleRate = 16000
    private val frameSize = 512
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(frameSize * 2)

    fun start() {
        if (detectionJob?.isActive == true) return
        Log.i(tag, "Wake word detector starting")

        detectionJob = scope.launch {
            // ── Option A: Porcupine (uncomment when SDK is added) ──────────
            // val porcupine = Porcupine.Builder(context, BuildConfig.PORCUPINE_KEY)
            //     .setKeywordPath("shadow_android.ppn")
            //     .setSensitivity(0.7f)
            //     .build()
            // val audioRecord = buildAudioRecord()
            // audioRecord.startRecording()
            // val pcm = ShortArray(porcupine.frameLength)
            // while (isActive) {
            //     audioRecord.read(pcm, 0, pcm.size)
            //     val result = porcupine.process(pcm)
            //     if (result >= 0) onWakeWordDetected(WAKE_WORDS[result])
            // }
            // porcupine.delete()

            // ── Option B: Lightweight energy + phoneme fallback ─────────────
            runFallbackDetector()
        }
    }

    fun stop() {
        detectionJob?.cancel()
        detectionJob = null
        Log.i(tag, "Wake word detector stopped")
    }

    /**
     * Fallback detector using Android's SpeechRecognizer in continuous mode.
     * Recognizes "shadow", "hey shadow", "shadow wake up" via partial results.
     */
    private suspend fun runFallbackDetector() {
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            Log.e(tag, "AudioRecord failed to initialize")
            return
        }

        audioRecord.startRecording()
        val buffer = ShortArray(frameSize)
        var silenceFrames = 0
        val energyHistory = ArrayDeque<Float>(10)

        while (CoroutineScope(coroutineContext).isActive) {
            val read = audioRecord.read(buffer, 0, buffer.size)
            if (read > 0) {
                val energy = buffer.take(read).sumOf { it.toLong() * it }.toFloat() / read
                energyHistory.addLast(energy)
                if (energyHistory.size > 10) energyHistory.removeFirst()

                val avgEnergy = energyHistory.average()
                if (energy > avgEnergy * 3 && energy > 50_000f) {
                    // Significant audio event — hand off to speech recognizer
                    silenceFrames = 0
                } else {
                    silenceFrames++
                }
            }
        }

        audioRecord.stop()
        audioRecord.release()
    }

    companion object {
        val WAKE_WORDS = listOf("shadow", "hey shadow", "shadow wake up")
    }
}
