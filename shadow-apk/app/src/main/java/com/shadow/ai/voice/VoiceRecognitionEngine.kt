package com.shadow.ai.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Single-shot voice recognition engine.
 * Called once after the wake word fires; listens until speech ends.
 *
 * Uses Android SpeechRecognizer (requires internet by default).
 * For offline: swap in Vosk (vosk-api.org) using the same interface.
 */
class VoiceRecognitionEngine(private val context: Context) {

    private val tag = "VoiceRecognitionEngine"

    suspend fun recognize(
        timeoutMs: Long = 8000L,
        onListening: () -> Unit = {}
    ): String? = withTimeoutOrNull(timeoutMs) {
        suspendCancellableCoroutine { cont ->
            listenOnce(cont, onListening)
        }
    }

    private fun listenOnce(
        cont: CancellableContinuation<String?>,
        onListening: () -> Unit
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e(tag, "SpeechRecognizer not available on this device")
            cont.resume(null)
            return
        }

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                onListening()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val best = matches?.firstOrNull()
                Log.d(tag, "Recognized: $best")
                recognizer.destroy()
                cont.resume(best)
            }

            override fun onError(error: Int) {
                Log.w(tag, "Recognition error: $error")
                recognizer.destroy()
                cont.resume(null)
            }

            override fun onPartialResults(partial: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onEndOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
        }

        recognizer.startListening(intent)

        cont.invokeOnCancellation {
            recognizer.stopListening()
            recognizer.destroy()
        }
    }
}
