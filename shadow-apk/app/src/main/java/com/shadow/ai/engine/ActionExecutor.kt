package com.shadow.ai.engine

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.provider.AlarmClock
import android.provider.Settings
import android.util.Log
import com.shadow.ai.models.ActionResult
import com.shadow.ai.models.ActionType
import com.shadow.ai.models.ShadowAction
import com.shadow.ai.services.ShadowAccessibilityService
import com.shadow.ai.vision.OCREngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Central dispatcher: receives a ShadowAction and executes it.
 * Coordinates between AppController, SmartClickEngine, OCREngine, and
 * direct Android API calls.
 */
class ActionExecutor(
    private val context: Context,
    private val appController: AppController,
    private val routineEngine: RoutineEngine,
    private val ocrEngine: OCREngine
) {
    private val tag = "ActionExecutor"

    suspend fun execute(action: ShadowAction): ActionResult = withContext(Dispatchers.Main) {
        Log.i(tag, "Executing: ${action.action} | app=${action.app} | target=${action.target}")
        try {
            when (action.action) {
                ActionType.OPEN_APP -> appController.openApp(action.app ?: "")
                ActionType.CLOSE_APP -> appController.closeCurrentApp()
                ActionType.PRESS_BACK -> ShadowAccessibilityService.instance?.performGlobalBack()
                    .let { ActionResult(true, "Back pressed") }
                ActionType.PRESS_HOME -> ShadowAccessibilityService.instance?.performGlobalHome()
                    .let { ActionResult(true, "Home pressed") }
                ActionType.PRESS_RECENTS -> ShadowAccessibilityService.instance?.performGlobalRecents()
                    .let { ActionResult(true, "Recents opened") }
                ActionType.CLICK_UI -> {
                    val a = ShadowAccessibilityService.instance
                    if (a != null && action.target != null) {
                        val found = a.smartClick(action.target)
                        ActionResult(found, if (found) "Clicked: ${action.target}" else "Element not found: ${action.target}")
                    } else ActionResult(false, "Accessibility service not connected")
                }
                ActionType.LONG_CLICK_UI -> {
                    val a = ShadowAccessibilityService.instance
                    if (a != null && action.target != null) {
                        val found = a.smartLongClick(action.target)
                        ActionResult(found, if (found) "Long-clicked: ${action.target}" else "Element not found")
                    } else ActionResult(false, "Accessibility service not connected")
                }
                ActionType.TYPE_TEXT -> {
                    val a = ShadowAccessibilityService.instance
                    if (a != null && action.text != null) {
                        a.typeText(action.text)
                        ActionResult(true, "Typed: ${action.text}")
                    } else ActionResult(false, "No text or service unavailable")
                }
                ActionType.SWIPE -> {
                    val a = ShadowAccessibilityService.instance
                    if (a != null && action.direction != null) {
                        a.performSwipe(action.direction)
                        ActionResult(true, "Swiped ${action.direction}")
                    } else ActionResult(false, "Accessibility service not connected")
                }
                ActionType.SCROLL_DOWN -> {
                    ShadowAccessibilityService.instance?.performScrollDown()
                    ActionResult(true, "Scrolled down")
                }
                ActionType.SCROLL_UP -> {
                    ShadowAccessibilityService.instance?.performScrollUp()
                    ActionResult(true, "Scrolled up")
                }
                ActionType.TOGGLE_FLASHLIGHT -> toggleFlashlight(action.state ?: true)
                ActionType.TOGGLE_WIFI -> toggleWifi(action.state ?: true)
                ActionType.TOGGLE_BLUETOOTH -> toggleBluetooth(action.state ?: true)
                ActionType.TOGGLE_DND -> toggleDnd(action.state ?: true)
                ActionType.TOGGLE_SILENT -> toggleSilent(action.state ?: true)
                ActionType.TOGGLE_AIRPLANE -> openAirplaneSettings()
                ActionType.SET_BRIGHTNESS -> setBrightness(action.value?.toIntOrNull() ?: 50)
                ActionType.SET_VOLUME -> setVolume(action.value?.toIntOrNull() ?: 50)
                ActionType.OCR_READ_SCREEN -> {
                    val text = ocrEngine.readCurrentScreen()
                    ActionResult(true, "Screen read", outputText = text)
                }
                ActionType.TAKE_SCREENSHOT -> {
                    ActionResult(true, "Screenshot taken")
                }
                ActionType.SEND_MESSAGE -> sendWhatsAppMessage(action.contact, action.message)
                ActionType.OPEN_WHATSAPP -> appController.openApp("WhatsApp")
                ActionType.MAKE_CALL -> makeCall(action.contact)
                ActionType.PLAY_MUSIC -> appController.openApp("Spotify")
                ActionType.PAUSE_MUSIC -> {
                    val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    audio.dispatchMediaKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_MEDIA_PAUSE))
                    ActionResult(true, "Music paused")
                }
                ActionType.NEXT_TRACK -> {
                    val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    audio.dispatchMediaKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_MEDIA_NEXT))
                    ActionResult(true, "Next track")
                }
                ActionType.SEARCH_WEB -> {
                    val uri = Uri.parse("https://google.com/search?q=${Uri.encode(action.query ?: "")}")
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    ActionResult(true, "Searching: ${action.query}")
                }
                ActionType.OPEN_URL -> {
                    val uri = Uri.parse(action.url ?: "https://google.com")
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    ActionResult(true, "Opened: ${action.url}")
                }
                ActionType.SET_ALARM -> setAlarm(action.value ?: "")
                ActionType.RUN_ROUTINE -> routineEngine.runRoutine(action.routine ?: "")
                ActionType.CLEAR_TEXT -> {
                    ShadowAccessibilityService.instance?.clearFocusedText()
                    ActionResult(true, "Text cleared")
                }
                ActionType.SWITCH_APP -> appController.openApp(action.app ?: "")
                ActionType.UNKNOWN -> ActionResult(false, "Command not understood. Try again.")
            }
        } catch (e: Exception) {
            Log.e(tag, "Action failed: ${action.action}", e)
            ActionResult(false, "Error: ${e.message}")
        }
    }

    private fun toggleFlashlight(on: Boolean): ActionResult {
        return try {
            val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cm.cameraIdList.firstOrNull() ?: return ActionResult(false, "No camera found")
            cm.setTorchMode(cameraId, on)
            ActionResult(true, "Flashlight ${if (on) "on" else "off"}")
        } catch (e: Exception) {
            ActionResult(false, "Flashlight error: ${e.message}")
        }
    }

    private fun toggleWifi(on: Boolean): ActionResult {
        return try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            ActionResult(true, "WiFi settings opened — toggle ${if (on) "on" else "off"}")
        } catch (e: Exception) {
            ActionResult(false, "WiFi error: ${e.message}")
        }
    }

    private fun toggleBluetooth(on: Boolean): ActionResult {
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return ActionResult(true, "Bluetooth settings opened")
    }

    private fun toggleDnd(on: Boolean): ActionResult {
        val intent = Intent(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            ActionResult(true, "DND settings opened")
        } catch (e: Exception) {
            ActionResult(false, "DND not supported: ${e.message}")
        }
    }

    private fun toggleSilent(on: Boolean): ActionResult {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audio.ringerMode = if (on) AudioManager.RINGER_MODE_SILENT else AudioManager.RINGER_MODE_NORMAL
        return ActionResult(true, "Ringer mode: ${if (on) "silent" else "normal"}")
    }

    private fun openAirplaneSettings(): ActionResult {
        context.startActivity(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        return ActionResult(true, "Airplane mode settings opened")
    }

    private fun setBrightness(level: Int): ActionResult {
        return try {
            val clamped = level.coerceIn(0, 100)
            val raw = (clamped / 100f * 255).toInt()
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, raw)
            ActionResult(true, "Brightness set to $clamped%")
        } catch (e: Exception) {
            ActionResult(false, "Write settings permission required")
        }
    }

    private fun setVolume(level: Int): ActionResult {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val target = (level / 100f * max).toInt().coerceIn(0, max)
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
        return ActionResult(true, "Volume set to $level%")
    }

    private suspend fun sendWhatsAppMessage(contact: String?, message: String?): ActionResult {
        if (contact == null || message == null) return ActionResult(false, "Contact or message missing")
        return try {
            val pm = context.packageManager
            pm.getPackageInfo("com.whatsapp", 0)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=&text=${Uri.encode(message)}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            delay(2000)
            // After opening WhatsApp, accessibility service will find the contact and send
            ShadowAccessibilityService.instance?.smartClick(contact)
            delay(1000)
            ShadowAccessibilityService.instance?.typeText(message)
            delay(500)
            ShadowAccessibilityService.instance?.smartClick("Send")
            ActionResult(true, "Message sent to $contact")
        } catch (e: Exception) {
            ActionResult(false, "WhatsApp not installed or error: ${e.message}")
        }
    }

    private fun makeCall(contact: String?): ActionResult {
        if (contact.isNullOrBlank()) return ActionResult(false, "No contact specified")
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ActionResult(true, "Dialer opened for $contact")
    }

    private fun setAlarm(timeText: String): ActionResult {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, "SHADOW Alarm")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ActionResult(true, "Alarm set")
    }
}
