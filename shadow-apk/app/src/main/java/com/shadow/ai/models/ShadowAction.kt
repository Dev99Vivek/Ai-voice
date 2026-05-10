package com.shadow.ai.models

import com.google.gson.annotations.SerializedName

/**
 * Canonical structured action emitted by SHADOW's intent processor.
 * Every spoken command resolves to one or more ShadowAction objects
 * that are dispatched to ActionExecutor.
 */
data class ShadowAction(
    @SerializedName("action") val action: ActionType,
    @SerializedName("app") val app: String? = null,
    @SerializedName("target") val target: String? = null,
    @SerializedName("text") val text: String? = null,
    @SerializedName("state") val state: Boolean? = null,
    @SerializedName("value") val value: String? = null,
    @SerializedName("routine") val routine: String? = null,
    @SerializedName("query") val query: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("direction") val direction: SwipeDirection? = null,
    @SerializedName("contact") val contact: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("confirm") val confirm: Boolean = false
)

enum class ActionType {
    // App control
    OPEN_APP,
    CLOSE_APP,
    SWITCH_APP,

    // UI actions
    CLICK_UI,
    LONG_CLICK_UI,
    SWIPE,
    SCROLL_DOWN,
    SCROLL_UP,
    TYPE_TEXT,
    CLEAR_TEXT,
    PRESS_BACK,
    PRESS_HOME,
    PRESS_RECENTS,

    // System toggles
    TOGGLE_WIFI,
    TOGGLE_BLUETOOTH,
    TOGGLE_FLASHLIGHT,
    TOGGLE_DND,
    TOGGLE_AIRPLANE,
    TOGGLE_SILENT,
    SET_BRIGHTNESS,
    SET_VOLUME,

    // Screen
    OCR_READ_SCREEN,
    TAKE_SCREENSHOT,

    // Communication
    OPEN_WHATSAPP,
    SEND_MESSAGE,
    MAKE_CALL,

    // Routines
    RUN_ROUTINE,

    // Media
    PLAY_MUSIC,
    PAUSE_MUSIC,
    NEXT_TRACK,

    // Misc
    OPEN_URL,
    SEARCH_WEB,
    SET_ALARM,
    UNKNOWN
}

enum class SwipeDirection {
    UP, DOWN, LEFT, RIGHT
}

/** Result of executing a ShadowAction */
data class ActionResult(
    val success: Boolean,
    val message: String,
    val outputText: String? = null,
    val requiresConfirm: Boolean = false
)

/** Built-in routines */
data class Routine(
    val name: String,
    val displayName: String,
    val actions: List<ShadowAction>
)

val DEFAULT_ROUTINES = listOf(
    Routine(
        name = "gaming_mode",
        displayName = "Gaming Mode",
        actions = listOf(
            ShadowAction(ActionType.OPEN_APP, app = "Discord"),
            ShadowAction(ActionType.TOGGLE_DND, state = true),
            ShadowAction(ActionType.SET_BRIGHTNESS, value = "80"),
            ShadowAction(ActionType.SET_VOLUME, value = "100")
        )
    ),
    Routine(
        name = "study_mode",
        displayName = "Study Mode",
        actions = listOf(
            ShadowAction(ActionType.TOGGLE_SILENT, state = true),
            ShadowAction(ActionType.TOGGLE_DND, state = true),
            ShadowAction(ActionType.SET_BRIGHTNESS, value = "60"),
            ShadowAction(ActionType.OPEN_APP, app = "Notes")
        )
    ),
    Routine(
        name = "sleep_mode",
        displayName = "Sleep Mode",
        actions = listOf(
            ShadowAction(ActionType.TOGGLE_WIFI, state = false),
            ShadowAction(ActionType.TOGGLE_SILENT, state = true),
            ShadowAction(ActionType.SET_BRIGHTNESS, value = "0"),
            ShadowAction(ActionType.TOGGLE_DND, state = true)
        )
    )
)
