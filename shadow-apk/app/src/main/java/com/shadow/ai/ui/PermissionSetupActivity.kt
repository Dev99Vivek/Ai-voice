package com.shadow.ai.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.shadow.ai.services.ShadowForegroundService

/**
 * Transparent trampoline activity used when SHADOW needs to request a
 * specific permission from a non-Activity context (e.g. from the service).
 *
 * Currently used for future OAuth / deep-link flows.
 * The main permission setup is handled in MainActivity.
 */
class PermissionSetupActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent?.action) {
            ACTION_OPEN_ACCESSIBILITY -> {
                startActivity(PermissionHelper.accessibilitySettingsIntent())
                finish()
            }
            ACTION_OPEN_OVERLAY -> {
                startActivity(PermissionHelper.overlaySettingsIntent(this))
                finish()
            }
            ACTION_OPEN_BATTERY -> {
                startActivity(PermissionHelper.batteryOptimizationIntent(this))
                finish()
            }
            ACTION_RESTART_SHADOW -> {
                startForegroundService(Intent(this, ShadowForegroundService::class.java))
                finish()
            }
            else -> finish()
        }
    }

    companion object {
        const val ACTION_OPEN_ACCESSIBILITY = "com.shadow.ai.ACTION_OPEN_ACCESSIBILITY"
        const val ACTION_OPEN_OVERLAY = "com.shadow.ai.ACTION_OPEN_OVERLAY"
        const val ACTION_OPEN_BATTERY = "com.shadow.ai.ACTION_OPEN_BATTERY"
        const val ACTION_RESTART_SHADOW = "com.shadow.ai.ACTION_RESTART_SHADOW"

        fun accessibilityIntent(context: android.content.Context) =
            Intent(context, PermissionSetupActivity::class.java).apply {
                action = ACTION_OPEN_ACCESSIBILITY
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
    }
}
