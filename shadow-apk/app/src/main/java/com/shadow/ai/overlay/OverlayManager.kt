package com.shadow.ai.overlay

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.shadow.ai.R

/**
 * Floating SHADOW overlay bubble.
 *
 * Renders above all apps using SYSTEM_ALERT_WINDOW.
 * Features:
 *  - Draggable "S" bubble with neon glow
 *  - State-driven UI: IDLE / LISTENING / PROCESSING / DONE
 *  - Transcript panel that fades in/out
 *  - Snaps to nearest screen edge on release
 */
class OverlayManager(private val context: Context) {

    enum class State { IDLE, LISTENING, PROCESSING, DONE }

    private val tag = "OverlayManager"
    private val wm: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: View? = null
    private var transcriptView: TextView? = null
    private var stateIndicator: View? = null
    private var currentState = State.IDLE
    private var pulseAnimator: ValueAnimator? = null

    private val overlayParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.BOTTOM or Gravity.END
        x = 32
        y = 120
    }

    fun show() {
        if (overlayView != null) return
        try {
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.overlay_shadow_bubble, null)
            overlayView = view

            transcriptView = view.findViewById(R.id.tv_transcript)
            stateIndicator = view.findViewById(R.id.view_state_indicator)

            makeDraggable(view)
            wm.addView(view, overlayParams)
            setState(State.IDLE)
            Log.i(tag, "Overlay shown")
        } catch (e: Exception) {
            Log.e(tag, "Failed to show overlay: ${e.message}")
        }
    }

    fun hide() {
        overlayView?.let {
            try {
                wm.removeView(it)
            } catch (_: Exception) {}
            overlayView = null
        }
        pulseAnimator?.cancel()
        Log.i(tag, "Overlay hidden")
    }

    fun setState(state: State) {
        currentState = state
        val view = overlayView ?: return
        val bubble = view.findViewById<View>(R.id.view_bubble) ?: return

        pulseAnimator?.cancel()

        when (state) {
            State.IDLE -> {
                bubble.alpha = 0.7f
                stateIndicator?.setBackgroundResource(R.color.overlay_idle)
            }
            State.LISTENING -> {
                stateIndicator?.setBackgroundResource(R.color.overlay_listening)
                pulseAnimator = ValueAnimator.ofFloat(0.7f, 1f).apply {
                    duration = 600
                    repeatMode = ValueAnimator.REVERSE
                    repeatCount = ValueAnimator.INFINITE
                    addUpdateListener { bubble.alpha = it.animatedValue as Float }
                    start()
                }
            }
            State.PROCESSING -> {
                stateIndicator?.setBackgroundResource(R.color.overlay_processing)
                bubble.alpha = 1f
            }
            State.DONE -> {
                stateIndicator?.setBackgroundResource(R.color.overlay_done)
                bubble.alpha = 1f
            }
        }
    }

    fun showTranscript(text: String) {
        val tv = transcriptView ?: return
        tv.text = text
        tv.visibility = View.VISIBLE
        tv.animate().alpha(1f).setDuration(200).start()

        // Auto-hide after 4 seconds
        tv.postDelayed({
            tv.animate().alpha(0f).setDuration(500)
                .withEndAction { tv.visibility = View.GONE }.start()
        }, 4000)
    }

    private fun makeDraggable(view: View) {
        var lastX = 0f
        var lastY = 0f
        var initialTouchX = 0f
        var initialTouchY = 0f

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = overlayParams.x.toFloat()
                    lastY = overlayParams.y.toFloat()
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    overlayParams.x = (lastX + (initialTouchX - event.rawX)).toInt()
                    overlayParams.y = (lastY + (event.rawY - initialTouchY)).toInt()
                    try { wm.updateViewLayout(view, overlayParams) } catch (_: Exception) {}
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val moved = Math.abs(event.rawX - initialTouchX) + Math.abs(event.rawY - initialTouchY)
                    if (moved < 10) {
                        // Treat as tap — toggle transcript visibility
                        val tv = transcriptView
                        if (tv != null && tv.visibility == View.VISIBLE) {
                            tv.animate().alpha(0f).setDuration(300)
                                .withEndAction { tv.visibility = View.GONE }.start()
                        }
                    } else {
                        // Snap to nearest edge
                        snapToEdge(view)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun snapToEdge(view: View) {
        val dm = context.resources.displayMetrics
        val mid = dm.widthPixels / 2
        overlayParams.x = if (overlayParams.x < mid) 32 else 32
        try { wm.updateViewLayout(view, overlayParams) } catch (_: Exception) {}
    }
}
