package com.shadow.ai.vision

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.shadow.ai.services.ShadowAccessibilityService
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Screen OCR using ML Kit on-device text recognition.
 * Reads the current screen via the accessibility screenshot API.
 * No internet required — runs fully offline.
 */
class OCREngine(private val context: Context) {

    private val tag = "OCREngine"
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Read all visible text from the current screen.
     * Uses the accessibility service to capture a screenshot.
     */
    suspend fun readCurrentScreen(): String {
        val bitmap = captureScreen() ?: return "Could not capture screen"
        return recognizeText(bitmap)
    }

    /**
     * Run OCR on a specific bitmap (e.g. from media projection).
     */
    suspend fun recognizeText(bitmap: Bitmap): String = suspendCancellableCoroutine { cont ->
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { result ->
                val text = result.textBlocks
                    .joinToString("\n") { block -> block.text }
                    .trim()
                Log.d(tag, "OCR result: ${text.take(200)}...")
                cont.resume(text.ifEmpty { "No readable text found on screen" })
            }
            .addOnFailureListener { e ->
                Log.e(tag, "OCR failed", e)
                cont.resume("OCR error: ${e.message}")
            }
    }

    /**
     * Check if a specific string appears on screen.
     */
    suspend fun screenContains(query: String): Boolean {
        val text = readCurrentScreen()
        return text.contains(query, ignoreCase = true)
    }

    /**
     * Extract OTP from screen text (common 4-8 digit codes).
     */
    suspend fun extractOTP(): String? {
        val text = readCurrentScreen()
        val otpPattern = Regex("\\b(\\d{4,8})\\b")
        return otpPattern.find(text)?.value
    }

    /**
     * Capture screen bitmap via the accessibility service.
     * Requires Android 11+ for takeScreenshot API.
     */
    private suspend fun captureScreen(): Bitmap? {
        return ShadowAccessibilityService.instance?.captureScreenshot()
    }

    fun release() {
        recognizer.close()
    }
}
