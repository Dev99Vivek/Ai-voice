package com.shadow.ai.engine

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.shadow.ai.models.ActionResult

/**
 * Resolves app names to package names and launches them.
 * Maintains a fuzzy alias table so voice commands like
 * "open insta" correctly launch com.instagram.android.
 */
class AppController(private val context: Context) {

    private val tag = "AppController"
    private val pm: PackageManager = context.packageManager

    private val aliasMap = mapOf(
        "instagram" to "com.instagram.android",
        "insta" to "com.instagram.android",
        "whatsapp" to "com.whatsapp",
        "youtube" to "com.google.android.youtube",
        "yt" to "com.google.android.youtube",
        "spotify" to "com.spotify.music",
        "chrome" to "com.android.chrome",
        "browser" to "com.android.chrome",
        "telegram" to "org.telegram.messenger",
        "twitter" to "com.twitter.android",
        "x" to "com.twitter.android",
        "snapchat" to "com.snapchat.android",
        "snap" to "com.snapchat.android",
        "tiktok" to "com.zhiliaoapp.musically",
        "facebook" to "com.facebook.katana",
        "fb" to "com.facebook.katana",
        "maps" to "com.google.android.apps.maps",
        "google maps" to "com.google.android.apps.maps",
        "camera" to "com.android.camera2",
        "gallery" to "com.google.android.apps.photos",
        "photos" to "com.google.android.apps.photos",
        "settings" to "com.android.settings",
        "calculator" to "com.google.android.calculator",
        "clock" to "com.google.android.deskclock",
        "alarm" to "com.google.android.deskclock",
        "phone" to "com.google.android.dialer",
        "dialer" to "com.google.android.dialer",
        "messages" to "com.google.android.apps.messaging",
        "sms" to "com.google.android.apps.messaging",
        "gmail" to "com.google.android.gm",
        "email" to "com.google.android.gm",
        "drive" to "com.google.android.apps.docs",
        "google drive" to "com.google.android.apps.docs",
        "meet" to "com.google.android.apps.meetings",
        "zoom" to "us.zoom.videomeetings",
        "discord" to "com.discord",
        "netflix" to "com.netflix.mediaclient",
        "amazon" to "in.amazon.mShop.android.shopping",
        "flipkart" to "com.flipkart.android",
        "paytm" to "net.one97.paytm",
        "gpay" to "com.google.android.apps.nbu.paisa.user",
        "google pay" to "com.google.android.apps.nbu.paisa.user",
        "phonepe" to "com.phonepe.app",
        "bgmi" to "com.pubg.imobile",
        "pubg" to "com.pubg.imobile",
        "notes" to "com.google.android.keep",
        "keep" to "com.google.android.keep",
        "shorts" to "com.google.android.youtube",
        "reels" to "com.instagram.android",
        "files" to "com.google.android.documentsui",
        "file manager" to "com.google.android.documentsui",
        "contacts" to "com.google.android.contacts",
        "play store" to "com.android.vending",
        "store" to "com.android.vending",
        "linkedin" to "com.linkedin.android",
    )

    suspend fun openApp(name: String): ActionResult {
        val pkg = resolvePackage(name)
            ?: return ActionResult(false, "App not found: $name")

        return try {
            val launch = pm.getLaunchIntentForPackage(pkg)
                ?: return ActionResult(false, "$name is installed but has no launcher")
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            context.startActivity(launch)
            Log.i(tag, "Launched $name ($pkg)")
            ActionResult(true, "Opened $name")
        } catch (e: Exception) {
            Log.e(tag, "Failed to launch $pkg", e)
            ActionResult(false, "Could not open $name: ${e.message}")
        }
    }

    fun closeCurrentApp(): ActionResult {
        // Accessibility service performs back to home
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ActionResult(true, "App closed")
    }

    private fun resolvePackage(name: String): String? {
        val lower = name.lowercase().trim()

        // 1. Direct alias match
        aliasMap[lower]?.let { return it }

        // 2. Partial alias match
        aliasMap.entries.firstOrNull { lower.contains(it.key) || it.key.contains(lower) }
            ?.value?.let { return it }

        // 3. Fuzzy search through installed apps
        val installed = getInstalledApps()
        installed.firstOrNull { (label, _) ->
            label.lowercase().let { l -> l == lower || l.contains(lower) || lower.contains(l) }
        }?.second?.let { return it }

        return null
    }

    fun getInstalledApps(): List<Pair<String, String>> {
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
            .mapNotNull { info ->
                val label = pm.getApplicationLabel(info).toString()
                if (label.isNotBlank()) Pair(label, info.packageName) else null
            }
            .sortedBy { it.first }
    }
}
