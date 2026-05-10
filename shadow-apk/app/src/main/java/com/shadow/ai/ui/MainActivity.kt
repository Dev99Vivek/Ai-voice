package com.shadow.ai.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shadow.ai.services.ShadowForegroundService
import kotlinx.coroutines.delay

// ── Brand colors ──────────────────────────────────────────────────────────────
val Cyan = Color(0xFF00F5D4)
val Purple = Color(0xFFA855F7)
val DarkBg = Color(0xFF080C10)
val CardBg = Color(0xFF0D1520)
val Border = Color(0xFF1E3040)
val Muted = Color(0xFF5A7A8A)

class MainActivity : ComponentActivity() {

    private val micPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* permissions handled via PermissionHelper checks */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                background = DarkBg, surface = CardBg, primary = Cyan
            )) {
                ShadowOnboardingScreen(
                    onRequestMic = { requestMicPermission() },
                    onOpenOverlay = { startActivity(PermissionHelper.overlaySettingsIntent(this)) },
                    onOpenAccessibility = { startActivity(PermissionHelper.accessibilitySettingsIntent()) },
                    onOpenBattery = { startActivity(PermissionHelper.batteryOptimizationIntent(this)) },
                    onLaunch = { launchShadow() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh permission state each time activity resumes
    }

    private fun requestMicPermission() {
        val perms = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        micPermLauncher.launch(perms.toTypedArray())
    }

    private fun launchShadow() {
        val intent = Intent(this, ShadowForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        finish()
    }
}

@Composable
fun ShadowOnboardingScreen(
    onRequestMic: () -> Unit,
    onOpenOverlay: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenBattery: () -> Unit,
    onLaunch: () -> Unit
) {
    val context = LocalContext.current
    var status by remember { mutableStateOf(PermissionHelper.getStatus(context)) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
        while (true) {
            delay(1500)
            status = PermissionHelper.getStatus(context)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Background glow
        Box(
            Modifier
                .size(400.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-100).dp)
                .background(
                    Brush.radialGradient(listOf(Cyan.copy(alpha = 0.08f), Color.Transparent)),
                    CircleShape
                )
        )

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 }
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                ShadowLogo()
                Spacer(Modifier.height(8.dp))
                Text(
                    "Hands-free. Mind-fast.",
                    color = Muted,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Spacer(Modifier.height(48.dp))

                // Progress
                Text(
                    "SETUP",
                    color = Cyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 3.sp
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { status.progress },
                    modifier = Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(1.dp)),
                    color = Cyan,
                    trackColor = Border
                )
                Spacer(Modifier.height(32.dp))

                // Permission steps
                PermissionStep(
                    number = "01",
                    title = "Microphone",
                    description = "Required for wake word detection and voice commands",
                    granted = status.microphone,
                    onClick = onRequestMic
                )
                PermissionStep(
                    number = "02",
                    title = "Overlay Permission",
                    description = "Displays the SHADOW bubble above all apps",
                    granted = status.overlay,
                    onClick = onOpenOverlay
                )
                PermissionStep(
                    number = "03",
                    title = "Accessibility Service",
                    description = "Enables UI automation — tap, scroll, type in any app",
                    granted = status.accessibility,
                    onClick = onOpenAccessibility
                )
                PermissionStep(
                    number = "04",
                    title = "Battery Optimization",
                    description = "Keeps SHADOW running in background without interruption",
                    granted = status.batteryOptimization,
                    onClick = onOpenBattery
                )

                Spacer(Modifier.height(40.dp))

                // Launch button
                val canLaunch = status.microphone && status.overlay && status.accessibility
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (canLaunch)
                                Brush.horizontalGradient(listOf(Cyan, Purple))
                            else
                                Brush.horizontalGradient(listOf(Border, Border))
                        )
                        .clickable(enabled = canLaunch) { onLaunch() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (canLaunch) "ACTIVATE SHADOW" else "GRANT PERMISSIONS ABOVE",
                        color = if (canLaunch) Color.Black else Muted,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 2.sp
                    )
                }

                if (canLaunch) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Say \"Shadow\" to activate after launch",
                        color = Muted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(48.dp))

                // Wake words
                Text(
                    "WAKE WORDS",
                    color = Cyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 3.sp
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    listOf("Shadow", "Hey Shadow", "Shadow Wake Up").forEach { word ->
                        Text(
                            "\"$word\"",
                            color = Muted,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .border(1.dp, Border, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShadowLogo() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.08f, label = "pulse",
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse)
    )

    Box(contentAlignment = Alignment.Center) {
        // Outer glow ring
        Box(
            Modifier
                .size(100.dp)
                .scale(scale)
                .background(
                    Brush.radialGradient(listOf(Cyan.copy(alpha = 0.15f), Color.Transparent)),
                    CircleShape
                )
        )
        // Inner circle
        Box(
            Modifier
                .size(72.dp)
                .background(
                    Brush.linearGradient(listOf(Cyan, Purple)),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "S",
                color = Color.Black,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }

    Spacer(Modifier.height(16.dp))
    Text(
        "SHADOW",
        color = Color.White,
        fontSize = 28.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 6.sp
    )
}

@Composable
private fun PermissionStep(
    number: String,
    title: String,
    description: String,
    granted: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .border(
                1.dp,
                if (granted) Cyan.copy(alpha = 0.3f) else Border,
                RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !granted) { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Number badge
        Box(
            Modifier
                .size(36.dp)
                .background(
                    if (granted) Cyan.copy(alpha = 0.15f) else Border.copy(alpha = 0.4f),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (granted) "✓" else number,
                color = if (granted) Cyan else Muted,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = if (granted) Color.White else Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(2.dp))
            Text(
                description,
                color = Muted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(Modifier.width(12.dp))

        if (!granted) {
            Text(
                "GRANT →",
                color = Cyan,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}
