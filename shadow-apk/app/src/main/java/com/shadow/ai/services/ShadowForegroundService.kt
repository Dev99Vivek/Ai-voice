package com.shadow.ai.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.shadow.ai.R
import com.shadow.ai.ShadowApplication
import com.shadow.ai.engine.ActionExecutor
import com.shadow.ai.engine.AppController
import com.shadow.ai.engine.IntentProcessor
import com.shadow.ai.engine.RoutineEngine
import com.shadow.ai.models.ActionType
import com.shadow.ai.overlay.OverlayManager
import com.shadow.ai.ui.MainActivity
import com.shadow.ai.vision.OCREngine
import com.shadow.ai.voice.VoiceRecognitionEngine
import com.shadow.ai.voice.WakeWordDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * SHADOW's always-on background service.
 *
 * Lifecycle:
 *  1. Start foreground (shows persistent notification)
 *  2. Start wake word detector
 *  3. On wake word → start voice recognition
 *  4. On transcript → IntentProcessor → ActionExecutor
 *  5. Show result in overlay, update notification, record to memory
 */
class ShadowForegroundService : LifecycleService() {

    private val tag = "ShadowFGS"

    private lateinit var wakeWordDetector: WakeWordDetector
    private lateinit var voiceEngine: VoiceRecognitionEngine
    private lateinit var appController: AppController
    private lateinit var routineEngine: RoutineEngine
    private lateinit var ocrEngine: OCREngine
    private lateinit var executor: ActionExecutor
    private lateinit var overlayManager: OverlayManager
    private val memoryStore get() = (application as ShadowApplication).memoryStore

    private var isListening = false

    override fun onCreate() {
        super.onCreate()
        Log.i(tag, "ShadowForegroundService created")

        voiceEngine = VoiceRecognitionEngine(this)
        appController = AppController(this)
        routineEngine = RoutineEngine(this)
        ocrEngine = OCREngine(this)
        executor = ActionExecutor(this, appController, routineEngine, ocrEngine)
        routineEngine.setExecutor(executor)
        overlayManager = OverlayManager(this)

        wakeWordDetector = WakeWordDetector(this) { wakeWord ->
            if (!isListening) {
                Log.i(tag, "Wake word detected: \"$wakeWord\"")
                lifecycleScope.launch { handleWakeWord() }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startForeground(NOTIFICATION_ID, buildNotification("Listening for \"Shadow\"…"))
        wakeWordDetector.start()
        overlayManager.show()
        Log.i(tag, "SHADOW service started")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeWordDetector.stop()
        overlayManager.hide()
        ocrEngine.release()
        Log.i(tag, "SHADOW service destroyed")
    }

    // ── Wake word → Voice → Intent → Execute ─────────────────────────────

    private suspend fun handleWakeWord() = withContext(Dispatchers.Main) {
        isListening = true
        updateNotification("Listening…")
        overlayManager.setState(OverlayManager.State.LISTENING)

        val transcript = voiceEngine.recognize(
            timeoutMs = 8000L,
            onListening = { overlayManager.setState(OverlayManager.State.LISTENING) }
        )

        if (transcript.isNullOrBlank()) {
            Log.d(tag, "No speech detected")
            overlayManager.setState(OverlayManager.State.IDLE)
            updateNotification("Listening for \"Shadow\"…")
            isListening = false
            return@withContext
        }

        overlayManager.showTranscript("YOU: $transcript")
        updateNotification("Processing…")
        overlayManager.setState(OverlayManager.State.PROCESSING)

        val result = IntentProcessor.process(transcript)
        Log.d(tag, "Intent: ${result.actions.map { it.action }}")

        if (result.actions.firstOrNull()?.action == ActionType.UNKNOWN) {
            overlayManager.showTranscript("SHADOW: Command not understood")
            memoryStore.recordCommand(transcript, "UNKNOWN", false)
        } else {
            for (action in result.actions) {
                val actionResult = executor.execute(action)
                overlayManager.showTranscript("SHADOW: ${actionResult.message}")
                if (!actionResult.outputText.isNullOrBlank()) {
                    overlayManager.showTranscript(actionResult.outputText)
                }
                memoryStore.recordCommand(transcript, action.action.name, actionResult.success)
                delay(300)
            }
        }

        delay(2000)
        overlayManager.setState(OverlayManager.State.DONE)
        delay(1500)
        overlayManager.setState(OverlayManager.State.IDLE)
        updateNotification("Listening for \"Shadow\"…")
        isListening = false
    }

    // ── Notification ──────────────────────────────────────────────────────

    private fun buildNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, ShadowApplication.CHANNEL_MAIN)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_shadow_notification)
            .setContentIntent(pi)
            .setOngoing(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification(text))
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }
}
