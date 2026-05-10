package com.shadow.ai.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.shadow.ai.models.SwipeDirection
import kotlinx.coroutines.CompletableDeferred

/**
 * SHADOW Accessibility Service — the core automation engine.
 *
 * Provides:
 *  - Global navigation (back / home / recents)
 *  - Smart element finding by text, content description, class
 *  - Click, long-click, type, clear
 *  - Swipe and scroll gestures
 *  - Screen capture (Android 11+)
 *
 * Enable in: Settings → Accessibility → Installed Services → SHADOW AI
 */
class ShadowAccessibilityService : AccessibilityService() {

    private val tag = "ShadowA11y"

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.i(tag, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {
        Log.w(tag, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance === this) instance = null
        Log.i(tag, "Accessibility service destroyed")
    }

    // ── Global actions ────────────────────────────────────────────────────

    fun performGlobalBack() = performGlobalAction(GLOBAL_ACTION_BACK)
    fun performGlobalHome() = performGlobalAction(GLOBAL_ACTION_HOME)
    fun performGlobalRecents() = performGlobalAction(GLOBAL_ACTION_RECENTS)

    // ── Smart element finding ─────────────────────────────────────────────

    /**
     * Find the best matching UI element by label.
     * Searches text, content description, and class name.
     * Returns the highest-confidence match.
     */
    fun findElement(label: String): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        val lowerLabel = label.lowercase().trim()

        // Priority 1: exact text match
        root.findAccessibilityNodeInfosByText(label).firstOrNull()
            ?.let { return it }

        // Priority 2: case-insensitive contains
        val allNodes = mutableListOf<AccessibilityNodeInfo>()
        collectAll(root, allNodes)

        val exactContent = allNodes.firstOrNull { node ->
            node.contentDescription?.toString()?.lowercase() == lowerLabel
        }
        if (exactContent != null) return exactContent

        val partialText = allNodes.firstOrNull { node ->
            node.text?.toString()?.lowercase()?.contains(lowerLabel) == true
        }
        if (partialText != null) return partialText

        val partialContent = allNodes.firstOrNull { node ->
            node.contentDescription?.toString()?.lowercase()?.contains(lowerLabel) == true
        }
        return partialContent
    }

    /**
     * Tap the best matching UI element by label.
     */
    fun smartClick(label: String): Boolean {
        val node = findElement(label) ?: return false
        return if (node.isClickable) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            // Try clicking the closest clickable parent
            var parent = node.parent
            while (parent != null) {
                if (parent.isClickable) return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                parent = parent.parent
            }
            // Fallback: tap by coordinates
            val bounds = Rect()
            node.getBoundsInScreen(bounds)
            tapAt(bounds.centerX().toFloat(), bounds.centerY().toFloat())
        }
    }

    /**
     * Long press the best matching UI element.
     */
    fun smartLongClick(label: String): Boolean {
        val node = findElement(label) ?: return false
        return node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
    }

    // ── Text input ────────────────────────────────────────────────────────

    fun typeText(text: String): Boolean {
        val node = findFocusedInput() ?: findAnyInput()
        if (node != null) {
            val args = Bundle()
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        }
        return false
    }

    fun clearFocusedText(): Boolean {
        val node = findFocusedInput() ?: return false
        val args = Bundle()
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "")
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    private fun findFocusedInput(): AccessibilityNodeInfo? {
        return rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
    }

    private fun findAnyInput(): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        val nodes = mutableListOf<AccessibilityNodeInfo>()
        collectAll(root, nodes)
        return nodes.firstOrNull { it.isEditable }
    }

    // ── Scroll ────────────────────────────────────────────────────────────

    fun performScrollDown() {
        val root = rootInActiveWindow ?: return
        val nodes = mutableListOf<AccessibilityNodeInfo>()
        collectAll(root, nodes)
        val scrollable = nodes.firstOrNull { it.isScrollable }
        scrollable?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            ?: swipeVertical(0.7f, 0.3f)
    }

    fun performScrollUp() {
        val root = rootInActiveWindow ?: return
        val nodes = mutableListOf<AccessibilityNodeInfo>()
        collectAll(root, nodes)
        val scrollable = nodes.firstOrNull { it.isScrollable }
        scrollable?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
            ?: swipeVertical(0.3f, 0.7f)
    }

    // ── Swipe gestures ────────────────────────────────────────────────────

    fun performSwipe(direction: SwipeDirection) {
        when (direction) {
            SwipeDirection.UP -> swipeVertical(0.7f, 0.2f)
            SwipeDirection.DOWN -> swipeVertical(0.2f, 0.8f)
            SwipeDirection.LEFT -> swipeHorizontal(0.8f, 0.2f)
            SwipeDirection.RIGHT -> swipeHorizontal(0.2f, 0.8f)
        }
    }

    // ── Tap at absolute coordinates ────────────────────────────────────────

    fun tapAt(x: Float, y: Float): Boolean {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 100)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, null, null)
    }

    // ── Screen capture ────────────────────────────────────────────────────

    suspend fun captureScreenshot(): Bitmap? {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
            Log.w(tag, "Screenshot API requires Android 11+")
            return null
        }
        val deferred = CompletableDeferred<Bitmap?>()
        takeScreenshot(
            android.view.Display.DEFAULT_DISPLAY,
            mainExecutor,
            object : TakeScreenshotCallback {
                override fun onSuccess(result: ScreenshotResult) {
                    deferred.complete(Bitmap.wrapHardwareBuffer(result.hardwareBuffer, result.colorSpace)
                        ?.copy(Bitmap.Config.ARGB_8888, false))
                    result.hardwareBuffer.close()
                }
                override fun onFailure(errorCode: Int) {
                    Log.e(tag, "Screenshot failed: $errorCode")
                    deferred.complete(null)
                }
            }
        )
        return deferred.await()
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun swipeVertical(fromFrac: Float, toFrac: Float) {
        val dm = resources.displayMetrics
        val midX = dm.widthPixels / 2f
        val fromY = dm.heightPixels * fromFrac
        val toY = dm.heightPixels * toFrac
        val path = Path().apply {
            moveTo(midX, fromY)
            lineTo(midX, toY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, 300)
        dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
    }

    private fun swipeHorizontal(fromFrac: Float, toFrac: Float) {
        val dm = resources.displayMetrics
        val midY = dm.heightPixels / 2f
        val fromX = dm.widthPixels * fromFrac
        val toX = dm.widthPixels * toFrac
        val path = Path().apply {
            moveTo(fromX, midY)
            lineTo(toX, midY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, 300)
        dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
    }

    private fun collectAll(node: AccessibilityNodeInfo, list: MutableList<AccessibilityNodeInfo>) {
        list.add(node)
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { collectAll(it, list) }
        }
    }

    companion object {
        var instance: ShadowAccessibilityService? = null
            private set

        val isConnected: Boolean get() = instance != null
    }
}
