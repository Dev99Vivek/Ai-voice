package com.shadow.ai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.shadow.ai.data.MemoryStore
import com.shadow.ai.engine.RoutineEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class ShadowApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var memoryStore: MemoryStore
    lateinit var routineEngine: RoutineEngine

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
        memoryStore = MemoryStore(this)
        routineEngine = RoutineEngine(this)
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val mainChannel = NotificationChannel(
            CHANNEL_MAIN,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_desc)
            setShowBadge(false)
        }

        val alertChannel = NotificationChannel(
            CHANNEL_ALERTS,
            "SHADOW Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Action confirmations and alerts from SHADOW"
        }

        manager.createNotificationChannels(listOf(mainChannel, alertChannel))
    }

    companion object {
        const val CHANNEL_MAIN = "shadow_main"
        const val CHANNEL_ALERTS = "shadow_alerts"

        lateinit var instance: ShadowApplication
            private set
    }
}
