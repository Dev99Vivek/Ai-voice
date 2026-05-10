package com.shadow.ai.engine

import android.util.Log
import com.shadow.ai.models.ActionType
import com.shadow.ai.models.ShadowAction
import com.shadow.ai.models.SwipeDirection

/**
 * Converts raw voice transcript into one or more structured ShadowActions.
 *
 * This is a rule-based NLU engine. For advanced AI intent detection,
 * replace resolveIntent() with an LLM call (e.g. Gemini Nano on-device,
 * or an API call to your backend at /api/intent).
 */
object IntentProcessor {

    private const val TAG = "IntentProcessor"

    data class IntentResult(
        val actions: List<ShadowAction>,
        val transcript: String,
        val confidence: Float
    )

    fun process(transcript: String): IntentResult {
        val text = transcript.lowercase().trim()
        Log.d(TAG, "Processing: \"$text\"")

        val actions = resolveIntent(text)
        val confidence = if (actions.firstOrNull()?.action != ActionType.UNKNOWN) 0.9f else 0.3f

        return IntentResult(actions, transcript, confidence)
    }

    private fun resolveIntent(text: String): List<ShadowAction> {

        // ── App launching ─────────────────────────────────────────────────
        if (text.matches(Regex(".*(open|launch|start|run)\\s+(.+)"))) {
            val app = text.replace(Regex(".*(open|launch|start|run)\\s+"), "").trim()
            return listOf(ShadowAction(ActionType.OPEN_APP, app = app.toTitleCase()))
        }
        if (text.contains("close") || text.contains("exit")) {
            val app = text.replace(Regex(".*(close|exit)\\s*"), "").trim()
            return if (app.isNotEmpty())
                listOf(ShadowAction(ActionType.CLOSE_APP, app = app.toTitleCase()))
            else listOf(ShadowAction(ActionType.PRESS_BACK))
        }

        // ── Navigation ────────────────────────────────────────────────────
        if (text.contains("go back") || text.contains("press back")) {
            return listOf(ShadowAction(ActionType.PRESS_BACK))
        }
        if (text.contains("go home") || text.contains("home screen")) {
            return listOf(ShadowAction(ActionType.PRESS_HOME))
        }
        if (text.contains("recent") || text.contains("app switcher") || text.contains("multitask")) {
            return listOf(ShadowAction(ActionType.PRESS_RECENTS))
        }

        // ── Flashlight ────────────────────────────────────────────────────
        if (text.contains("flashlight") || text.contains("torch")) {
            val on = !text.contains("off") && !text.contains("turn off") && !text.contains("disable")
            return listOf(ShadowAction(ActionType.TOGGLE_FLASHLIGHT, state = on))
        }

        // ── WiFi ──────────────────────────────────────────────────────────
        if (text.contains("wifi") || text.contains("wi-fi")) {
            val on = !text.contains("off") && !text.contains("disable")
            return listOf(ShadowAction(ActionType.TOGGLE_WIFI, state = on))
        }

        // ── Bluetooth ─────────────────────────────────────────────────────
        if (text.contains("bluetooth")) {
            val on = !text.contains("off") && !text.contains("disable")
            return listOf(ShadowAction(ActionType.TOGGLE_BLUETOOTH, state = on))
        }

        // ── Do Not Disturb ────────────────────────────────────────────────
        if (text.contains("do not disturb") || text.contains("dnd")) {
            val on = !text.contains("off") && !text.contains("disable")
            return listOf(ShadowAction(ActionType.TOGGLE_DND, state = on))
        }

        // ── Silent mode ───────────────────────────────────────────────────
        if (text.contains("silent") || text.contains("mute")) {
            val on = !text.contains("off") && !text.contains("unmute")
            return listOf(ShadowAction(ActionType.TOGGLE_SILENT, state = on))
        }

        // ── Airplane mode ─────────────────────────────────────────────────
        if (text.contains("airplane") || text.contains("flight mode")) {
            val on = !text.contains("off") && !text.contains("disable")
            return listOf(ShadowAction(ActionType.TOGGLE_AIRPLANE, state = on))
        }

        // ── Brightness ───────────────────────────────────────────────────
        if (text.contains("brightness")) {
            val numMatch = Regex("(\\d+)").find(text)
            val level = numMatch?.value ?: if (text.contains("max") || text.contains("full")) "100"
            else if (text.contains("min") || text.contains("low")) "10" else "50"
            return listOf(ShadowAction(ActionType.SET_BRIGHTNESS, value = level))
        }

        // ── Volume ────────────────────────────────────────────────────────
        if (text.contains("volume")) {
            val numMatch = Regex("(\\d+)").find(text)
            val level = numMatch?.value ?: if (text.contains("max") || text.contains("full")) "100"
            else if (text.contains("min") || text.contains("low")) "0" else "50"
            return listOf(ShadowAction(ActionType.SET_VOLUME, value = level))
        }

        // ── OCR / read screen ─────────────────────────────────────────────
        if (text.contains("read") || text.contains("what does") || text.contains("what's on")) {
            return listOf(ShadowAction(ActionType.OCR_READ_SCREEN))
        }

        // ── Screenshot ───────────────────────────────────────────────────
        if (text.contains("screenshot") || text.contains("screen shot") || text.contains("capture screen")) {
            return listOf(ShadowAction(ActionType.TAKE_SCREENSHOT))
        }

        // ── Smart click (find and tap UI element) ─────────────────────────
        if (text.matches(Regex(".*(click|tap|press|find|hit)\\s+(.+)"))) {
            val target = text.replace(Regex(".*(click|tap|press|find|hit)\\s+"), "").trim()
                .replace(Regex("\\s*(button|icon|link|tab)$"), "").trim()
            return listOf(ShadowAction(ActionType.CLICK_UI, target = target))
        }

        // ── Type text ─────────────────────────────────────────────────────
        if (text.matches(Regex(".*(type|write|input|enter)\\s+(.+)"))) {
            val typed = text.replace(Regex(".*(type|write|input|enter)\\s+"), "").trim()
            return listOf(ShadowAction(ActionType.TYPE_TEXT, text = typed))
        }

        // ── Scroll ────────────────────────────────────────────────────────
        if (text.contains("scroll down") || text.contains("scroll up")) {
            return listOf(
                ShadowAction(
                    if (text.contains("up")) ActionType.SCROLL_UP else ActionType.SCROLL_DOWN
                )
            )
        }

        // ── Swipe ─────────────────────────────────────────────────────────
        if (text.contains("swipe")) {
            val dir = when {
                text.contains("left") -> SwipeDirection.LEFT
                text.contains("right") -> SwipeDirection.RIGHT
                text.contains("up") -> SwipeDirection.UP
                else -> SwipeDirection.DOWN
            }
            return listOf(ShadowAction(ActionType.SWIPE, direction = dir))
        }

        // ── WhatsApp message ─────────────────────────────────────────────
        if (text.contains("whatsapp") || text.contains("message") || text.contains("send") || text.contains("reply")) {
            val toMatch = Regex("(?:to|reply to|message)\\s+([a-zA-Z]+)").find(text)
            val msgMatch = Regex("(?:say|saying|message|send)\\s+[\"']?(.+?)[\"']?(?:\\s+to|$)").find(text)
            val contact = toMatch?.groupValues?.get(1)?.toTitleCase()
            val message = msgMatch?.groupValues?.get(1) ?: "okay"
            return if (contact != null) {
                listOf(
                    ShadowAction(ActionType.OPEN_APP, app = "WhatsApp"),
                    ShadowAction(ActionType.SEND_MESSAGE, contact = contact, message = message)
                )
            } else {
                listOf(ShadowAction(ActionType.OPEN_WHATSAPP))
            }
        }

        // ── Calls ─────────────────────────────────────────────────────────
        if (text.contains("call") || text.contains("dial")) {
            val contact = text.replace(Regex(".*(call|dial)\\s+"), "").trim()
            return listOf(ShadowAction(ActionType.MAKE_CALL, contact = contact.toTitleCase()))
        }

        // ── Music ─────────────────────────────────────────────────────────
        if (text.contains("play music") || text.contains("play song") || text.contains("play spotify")) {
            return listOf(ShadowAction(ActionType.PLAY_MUSIC))
        }
        if (text.contains("pause") || text.contains("stop music")) {
            return listOf(ShadowAction(ActionType.PAUSE_MUSIC))
        }
        if (text.contains("next song") || text.contains("next track") || text.contains("skip")) {
            return listOf(ShadowAction(ActionType.NEXT_TRACK))
        }

        // ── Web search ────────────────────────────────────────────────────
        if (text.contains("search for") || text.contains("google") || text.contains("search")) {
            val query = text.replace(Regex(".*(search for|google|search)\\s+"), "").trim()
            return listOf(ShadowAction(ActionType.SEARCH_WEB, query = query))
        }

        // ── URL ───────────────────────────────────────────────────────────
        if (text.contains("open") && (text.contains(".com") || text.contains(".in") || text.contains("website"))) {
            val urlMatch = Regex("([a-zA-Z0-9.-]+\\.(com|in|org|net|io))").find(text)
            val url = urlMatch?.value?.let { "https://$it" }
            return listOf(ShadowAction(ActionType.OPEN_URL, url = url))
        }

        // ── Alarm ─────────────────────────────────────────────────────────
        if (text.contains("alarm") || text.contains("set alarm") || text.contains("wake me")) {
            return listOf(ShadowAction(ActionType.SET_ALARM, value = text))
        }

        // ── Routines ─────────────────────────────────────────────────────
        if (text.contains("gaming mode") || text.contains("game mode")) {
            return listOf(ShadowAction(ActionType.RUN_ROUTINE, routine = "gaming_mode"))
        }
        if (text.contains("study mode")) {
            return listOf(ShadowAction(ActionType.RUN_ROUTINE, routine = "study_mode"))
        }
        if (text.contains("sleep mode")) {
            return listOf(ShadowAction(ActionType.RUN_ROUTINE, routine = "sleep_mode"))
        }

        // ── Fallback ──────────────────────────────────────────────────────
        return listOf(ShadowAction(ActionType.UNKNOWN))
    }

    private fun String.toTitleCase(): String =
        split(" ").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
}
