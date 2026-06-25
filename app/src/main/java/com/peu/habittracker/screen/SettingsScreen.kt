package com.peu.habittracker.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.peu.habittracker.viewModel.AccentColor
import com.peu.habittracker.viewModel.SettingsViewModel

// ─────────────────────────────────────────────────────────────────────────────
// DESIGN TOKENS  (shared across all screens)
// ─────────────────────────────────────────────────────────────────────────────
private val NightBase     = Color(0xFF0A0D1A)
private val CardDark      = Color(0xFF1A2035)
private val NeonViolet    = Color(0xFF7C6FFF)
private val NeonCyan      = Color(0xFF00D4FF)
private val NeonPink      = Color(0xFFFF6FD8)
private val NeonGreen     = Color(0xFF00F5A0)
private val NeonAmber     = Color(0xFFFFB800)
private val NeonRed       = Color(0xFFFF4757)
private val TextPrimary   = Color(0xFFF1F3FF)
private val TextSecondary = Color(0xFF8B90A8)
private val GlassWhite    = Color(0x14FFFFFF)
private val GlassStroke   = Color(0x26FFFFFF)

private val VioletGrad = listOf(NeonViolet, NeonCyan)
private val PinkGrad   = listOf(NeonPink, NeonViolet)
private val GreenGrad  = listOf(NeonGreen, Color(0xFF00B4DB))
private val AmberGrad  = listOf(NeonAmber, Color(0xFFFF6B00))
private val RedGrad    = listOf(NeonRed, NeonPink)
private val CyanGrad   = listOf(NeonCyan, NeonViolet)

// ─────────────────────────────────────────────────────────────────────────────
// SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToExport: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    // ── State collection ─────────────────────────────────────────────────────
    val notifEnabled  by viewModel.notificationsEnabled.collectAsState()
    val notifTime     by viewModel.notificationTime.collectAsState()
    val notifSound    by viewModel.notificationSound.collectAsState()
    val notifVibrate  by viewModel.notificationVibrate.collectAsState()
    val darkMode      by viewModel.darkMode.collectAsState()
    val accentIdx     by viewModel.accentColorIndex.collectAsState()
    val compactMode   by viewModel.compactMode.collectAsState()
    val streakAnim    by viewModel.showStreakAnimation.collectAsState()
    val weekMonday    by viewModel.weekStartMonday.collectAsState()
    val confetti      by viewModel.showConfetti.collectAsState()
    val analytics     by viewModel.analyticsEnabled.collectAsState()
    val crashReport   by viewModel.crashReporting.collectAsState()
    val autoBackup    by viewModel.autoBackup.collectAsState()

    // ── Dialog flags ─────────────────────────────────────────────────────────
    var showTimePicker   by remember { mutableStateOf(false) }
    var showClearDialog  by remember { mutableStateOf(false) }
    var showPrivacySheet by remember { mutableStateOf(false) }

    // ── Root ─────────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize().background(NightBase)) {
        SettingsOrbs()

        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {

                SettingsTopBar(onBack = onNavigateBack)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(Modifier.height(4.dp))

                    // ── Brand Hero ───────────────────────────────────────────
                    SettingsBrandHero()

                    // ════════════════════════════════════════════════════════
                    // 🔔 NOTIFICATIONS
                    // ════════════════════════════════════════════════════════
                    SettingsGroup("🔔  Notifications", VioletGrad) {
                        SToggle(Icons.Default.Notifications, VioletGrad,
                            "Daily Reminders", "Get nudged to complete your habits",
                            notifEnabled) { viewModel.toggleNotifications(it) }

                        AnimatedVisibility(notifEnabled,
                            enter = expandVertically() + fadeIn(),
                            exit  = shrinkVertically() + fadeOut()) {
                            Column {
                                SDivider()
                                SAction(Icons.Default.Schedule, AmberGrad,
                                    "Reminder Time", "When you'll be reminded daily",
                                    badge = formatTime(notifTime.first, notifTime.second)) {
                                    showTimePicker = true
                                }
                                SDivider()
                                SToggle(Icons.Default.VolumeUp, GreenGrad,
                                    "Sound", "Play alert sound with reminder",
                                    notifSound) { viewModel.toggleNotificationSound(it) }
                                SDivider()
                                SToggle(Icons.Default.Vibration, PinkGrad,
                                    "Vibration", "Vibrate when reminder fires",
                                    notifVibrate) { viewModel.toggleNotificationVibrate(it) }
                            }
                        }
                    }

                    // ════════════════════════════════════════════════════════
                    // 🎨 APPEARANCE
                    // ════════════════════════════════════════════════════════
                    SettingsGroup("🎨  Appearance", PinkGrad) {
                        SToggle(Icons.Default.DarkMode, PinkGrad,
                            "Dark Mode", "Always-on AMOLED dark theme",
                            darkMode) { viewModel.toggleDarkMode(it) }
                        SDivider()

                        // Inline accent color picker
                        AccentColorPicker(
                            selectedIndex = accentIdx,
                            onSelect      = { viewModel.setAccentColor(it) }
                        )
                        SDivider()

                        SToggle(Icons.Default.ViewCompact, CyanGrad,
                            "Compact Mode", "Smaller cards — fit more habits on screen",
                            compactMode) { viewModel.toggleCompactMode(it) }
                        SDivider()

                        SToggle(Icons.Default.Animation, VioletGrad,
                            "Streak Animations", "Animated fire & progress rings",
                            streakAnim) { viewModel.toggleStreakAnimation(it) }
                    }

                    // ════════════════════════════════════════════════════════
                    // ⚙️ HABIT PREFERENCES
                    // ════════════════════════════════════════════════════════
                    SettingsGroup("⚙️  Habit Preferences", AmberGrad) {
                        SToggle(Icons.Default.CalendarToday, AmberGrad,
                            "Week Starts Monday", "Toggle off for Sunday start",
                            weekMonday) { viewModel.toggleWeekStartMonday(it) }
                        SDivider()
                        SToggle(Icons.Default.Celebration, GreenGrad,
                            "Completion Confetti", "Celebrate when all habits are done 🎉",
                            confetti) { viewModel.toggleConfetti(it) }
                    }

                    // ════════════════════════════════════════════════════════
                    // 📦 DATA & PRIVACY
                    // ════════════════════════════════════════════════════════
                    SettingsGroup("📦  Data & Privacy", GreenGrad) {
                        SAction(Icons.Default.Download, GreenGrad,
                            "Export Data", "Save habits as CSV, JSON or TXT") {
                            onNavigateToExport()
                        }
                        SDivider()
                        SToggle(Icons.Default.Backup, CyanGrad,
                            "Auto Backup", "Back up data daily automatically",
                            autoBackup) { viewModel.toggleAutoBackup(it) }
                        SDivider()
                        SToggle(Icons.Default.BarChart, VioletGrad,
                            "Usage Analytics", "Anonymous — helps improve the app",
                            analytics) { viewModel.toggleAnalytics(it) }
                        SDivider()
                        SToggle(Icons.Default.BugReport, AmberGrad,
                            "Crash Reporting", "Send crash reports to fix bugs faster",
                            crashReport) { viewModel.toggleCrashReporting(it) }
                        SDivider()
                        SAction(Icons.Default.PrivacyTip, CyanGrad,
                            "Privacy Policy", "Read how we handle your data") {
                            showPrivacySheet = true
                        }
                        SDivider()
                        SAction(Icons.Default.DeleteForever, RedGrad,
                            "Clear All Data", "Permanently delete all habits & history",
                            isDanger = true) { showClearDialog = true }
                    }

                    // ════════════════════════════════════════════════════════
                    // 🌟 COMMUNITY
                    // ════════════════════════════════════════════════════════
                    SettingsGroup("🌟  Community", listOf(NeonAmber, NeonPink)) {
                        SAction(Icons.Default.Star, listOf(NeonAmber, NeonPink),
                            "Rate HabitTracker", "Love it? Leave us 5 stars ⭐") {
                            viewModel.openPlayStoreListing()
                        }
                        SDivider()
                        SAction(Icons.Default.Share, VioletGrad,
                            "Share with Friends", "Spread the habit-building love") {
                            viewModel.shareApp()
                        }
                        SDivider()
                        SAction(Icons.Default.Email, GreenGrad,
                            "Send Feedback", "Ideas, bugs, or just say hi 👋") {
                            viewModel.sendFeedbackEmail()
                        }
                    }

                    // ════════════════════════════════════════════════════════
                    // ℹ️ ABOUT
                    // ════════════════════════════════════════════════════════
                    SettingsGroup("ℹ️  About", AmberGrad) {
                        SInfo(Icons.Default.Info,         AmberGrad,  "Version",      "1.0.0 (Build 1)")
                        SDivider()
                        SInfo(Icons.Default.Code,         VioletGrad, "Built with",   "Jetpack Compose · Kotlin")
                        SDivider()
                        SInfo(Icons.Default.Architecture, CyanGrad,   "Architecture", "MVVM · Hilt · Room")
                        SDivider()
                        SInfo(Icons.Default.Copyright,    PinkGrad,   "License",      "© 2025 HabitTracker")
                    }
                }
            }
        }
    }

    // ── Dialogs / Sheets ──────────────────────────────────────────────────────
    if (showTimePicker) {
        DarkTimePickerDialog(
            initialHour   = notifTime.first,
            initialMinute = notifTime.second,
            onDismiss     = { showTimePicker = false },
            onConfirm     = { h, m ->
                viewModel.updateNotificationTime(h, m)
                showTimePicker = false
            }
        )
    }

    if (showClearDialog) {
        ClearDataDialog(
            onDismiss = { showClearDialog = false },
            onConfirm = { viewModel.clearAllData { showClearDialog = false } }
        )
    }

    if (showPrivacySheet) {
        PrivacyBottomSheet(onDismiss = { showPrivacySheet = false })
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AMBIENT ORBS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SettingsOrbs() {
    val inf = rememberInfiniteTransition(label = "sorbs")
    val t by inf.animateFloat(0f, 1f,
        infiniteRepeatable(tween(11000, easing = LinearEasing), RepeatMode.Reverse), label = "st")
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(listOf(NeonViolet.copy(0.13f + t * 0.05f), Color.Transparent),
                radius = size.width * 0.5f, center = Offset(size.width * 0.85f, size.height * 0.05f)),
            radius = size.width * 0.5f, center = Offset(size.width * 0.85f, size.height * 0.05f))
        drawCircle(
            brush = Brush.radialGradient(listOf(NeonPink.copy(0.08f), Color.Transparent),
                radius = size.width * 0.35f, center = Offset(0f, size.height * 0.4f)),
            radius = size.width * 0.35f, center = Offset(0f, size.height * 0.4f))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    Surface(color = NightBase, tonalElevation = 0.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape)
                .background(GlassWhite).border(1.dp, GlassStroke, CircleShape)
                .clickable(onClick = onBack), contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null,
                    tint = NeonViolet, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Settings", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = (-0.5).sp)
                Text("Customize your experience",
                    style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BRAND HERO CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SettingsBrandHero() {
    val inf = rememberInfiniteTransition(label = "brand")
    val pulse by inf.animateFloat(0.88f, 1f,
        infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bp")

    Box(modifier = Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(28.dp))
        .background(Brush.linearGradient(
            listOf(Color(0xFF1B1F3B), Color(0xFF0E1225)), Offset.Zero, Offset(1000f, 600f)))
        .border(1.dp, GlassStroke, RoundedCornerShape(28.dp))
        .padding(22.dp)
    ) {
        // Dot grid background
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 24.dp.toPx(); var x = step / 2
            while (x < size.width) {
                var y = step / 2
                while (y < size.height) {
                    drawCircle(Color.White.copy(0.03f), 1.5f, Offset(x, y)); y += step
                }; x += step
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Icon orb
            Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(Brush.radialGradient(listOf(NeonViolet.copy(0.3f * pulse), Color.Transparent)),
                        size.minDimension / 2)
                }
                Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(VioletGrad)), contentAlignment = Alignment.Center) {
                    Text("🎯", style = MaterialTheme.typography.titleLarge)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("HabitTracker", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = (-0.5).sp)
                Text("Build habits. Track progress. Win.",
                    style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SettingsBadge("v1.0.0", NeonViolet)
                    SettingsBadge("✨ Premium", NeonAmber)
                    SettingsBadge("🛡 Secure", NeonGreen)
                }
            }
        }
    }
}

@Composable
private fun SettingsBadge(text: String, color: Color) {
    Surface(shape = RoundedCornerShape(50.dp), color = color.copy(0.14f),
        modifier = Modifier.border(1.dp, color.copy(0.35f), RoundedCornerShape(50.dp))) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GROUP CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SettingsGroup(
    title: String,
    gradient: List<Color>,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Row(modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.width(3.dp).height(14.dp).clip(RoundedCornerShape(2.dp))
                .background(Brush.verticalGradient(gradient)))
            Text(title, style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(CardDark).border(1.dp, gradient[0].copy(0.2f), RoundedCornerShape(20.dp))) {
            Column { content() }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ACCENT COLOR PICKER  (inline inside appearance group)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AccentColorPicker(selectedIndex: Int, onSelect: (Int) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            SIconBox(Icons.Default.Palette, PinkGrad)
            Column(modifier = Modifier.weight(1f)) {
                Text("Accent Color", style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(AccentColor.entries.getOrNull(selectedIndex)?.label ?: "Violet × Cyan",
                    style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
        // Color swatches row
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AccentColor.entries.forEachIndexed { idx, ac ->
                val isSel = idx == selectedIndex
                val col   = Color(ac.hex)
                val size  by animateDpAsState(if (isSel) 40.dp else 34.dp,
                    spring(Spring.DampingRatioMediumBouncy), label = "csz$idx")
                Box(modifier = Modifier.size(size).clip(CircleShape)
                    .background(col)
                    .then(if (isSel) Modifier.border(3.dp, Color.White, CircleShape) else Modifier)
                    .clickable { onSelect(idx) },
                    contentAlignment = Alignment.Center) {
                    if (isSel) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOGGLE ROW  (short alias: SToggle)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SToggle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconGrad: List<Color>,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        SIconBox(icon, iconGrad)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        SNeonSwitch(checked = checked, onToggle = onToggle, gradient = iconGrad)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ACTION ROW  (short alias: SAction)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconGrad: List<Color>,
    title: String,
    subtitle: String,
    badge: String? = null,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
        .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp))
            .background(if (isDanger) Brush.linearGradient(listOf(NeonRed.copy(0.2f), NeonPink.copy(0.1f)))
            else Brush.linearGradient(listOf(iconGrad[0].copy(0.2f), iconGrad[1].copy(0.1f))))
            .border(1.dp, if (isDanger) NeonRed.copy(0.3f) else iconGrad[0].copy(0.3f), RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = if (isDanger) NeonRed else iconGrad[0], modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold, color = if (isDanger) NeonRed else TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        if (badge != null) {
            Surface(shape = RoundedCornerShape(50.dp), color = iconGrad[0].copy(0.12f),
                modifier = Modifier.border(1.dp, iconGrad[0].copy(0.3f), RoundedCornerShape(50.dp))) {
                Text(badge, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall, color = iconGrad[0], fontWeight = FontWeight.Bold)
            }
        } else {
            Icon(Icons.Default.ChevronRight, null,
                tint = if (isDanger) NeonRed.copy(0.5f) else TextSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// INFO ROW  (non-clickable value display, short alias: SInfo)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SInfo(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconGrad: List<Color>,
    title: String,
    value: String
) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        SIconBox(icon, iconGrad)
        Text(title, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.labelMedium,
            color = TextSecondary, fontWeight = FontWeight.Medium)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ICON BOX HELPER
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SIconBox(icon: androidx.compose.ui.graphics.vector.ImageVector, grad: List<Color>) {
    Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp))
        .background(Brush.linearGradient(listOf(grad[0].copy(0.2f), grad[1].copy(0.1f))))
        .border(1.dp, grad[0].copy(0.3f), RoundedCornerShape(11.dp)),
        contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = grad[0], modifier = Modifier.size(18.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NEON SWITCH
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SNeonSwitch(checked: Boolean, onToggle: (Boolean) -> Unit, gradient: List<Color>) {
    val pos by animateFloatAsState(if (checked) 1f else 0f,
        spring(Spring.DampingRatioMediumBouncy), label = "sw")
    val track  by animateColorAsState(if (checked) gradient[0].copy(0.3f) else GlassWhite, tween(200), label = "st")
    val border by animateColorAsState(if (checked) gradient[0].copy(0.6f) else GlassStroke, tween(200), label = "sb")
    Box(modifier = Modifier.width(48.dp).height(26.dp).clip(RoundedCornerShape(13.dp))
        .background(track).border(1.dp, border, RoundedCornerShape(13.dp))
        .clickable { onToggle(!checked) }) {
        Box(modifier = Modifier
            .offset(x = 3.dp + (22.dp * pos))
            .align(Alignment.CenterStart)
            .size(20.dp).clip(CircleShape)
            .background(if (checked) Brush.linearGradient(gradient)
            else Brush.linearGradient(listOf(TextSecondary.copy(0.5f), TextSecondary.copy(0.3f)))))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DIVIDER
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SDivider() {
    Box(modifier = Modifier.padding(start = 68.dp).fillMaxWidth()
        .height(1.dp).background(GlassStroke))
}

// ─────────────────────────────────────────────────────────────────────────────
// CLEAR DATA DIALOG  — full danger flow with item list
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ClearDataDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Color(0xFF141929),
        shape            = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(RedGrad)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.DeleteForever, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Text("Clear All Data?", color = NeonRed, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("This will permanently delete:",
                    color = TextPrimary, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold)
                listOf("All habits & their settings",
                    "Complete history & completions",
                    "Streaks, stats & achievements",
                    "All app preferences").forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(NeonRed))
                        Text(item, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Box(modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonRed.copy(0.08f))
                    .border(1.dp, NeonRed.copy(0.25f), RoundedCornerShape(12.dp))
                    .padding(12.dp)) {
                    Text("⚠️  This action cannot be undone.",
                        color = NeonRed, style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Box(modifier = Modifier.clip(RoundedCornerShape(12.dp))
                .background(Brush.horizontalGradient(RedGrad))
                .clickable(onClick = onConfirm)
                .padding(horizontal = 20.dp, vertical = 10.dp)) {
                Text("Delete Everything", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Box(modifier = Modifier.clip(RoundedCornerShape(12.dp))
                .background(GlassWhite).border(1.dp, GlassStroke, RoundedCornerShape(12.dp))
                .clickable(onClick = onDismiss).padding(horizontal = 20.dp, vertical = 10.dp)) {
                Text("Keep My Data", color = TextSecondary, fontWeight = FontWeight.Medium)
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// PRIVACY POLICY BOTTOM SHEET
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrivacyBottomSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = Color(0xFF141929),
        shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(13.dp))
                    .background(Brush.linearGradient(CyanGrad.map { it.copy(0.2f) }))
                    .border(1.dp, NeonCyan.copy(0.3f), RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PrivacyTip, null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text("Privacy Policy", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black, color = TextPrimary)
                    Text("Last updated: May 2025",
                        style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }

            listOf(
                Triple(Icons.Default.Storage,    NeonViolet, "Data Storage" to
                        "All habit data is stored locally on your device only. Nothing is uploaded to external servers without your consent."),
                Triple(Icons.Default.Analytics,  NeonCyan,   "Analytics" to
                        "If enabled, we collect anonymous usage events (screen visits, button taps). No habit names, notes, or personal data."),
                Triple(Icons.Default.BugReport,  NeonAmber,  "Crash Reporting" to
                        "Crash reports include device OS version and app state. No habit data, notes, or personally identifiable information."),
                Triple(Icons.Default.Block,      NeonGreen,  "No Ads or Selling" to
                        "HabitTracker is completely ad-free. We never sell or share your data with third parties for commercial purposes."),
                Triple(Icons.Default.AdminPanelSettings, NeonPink, "Your Rights" to
                        "Delete all data any time via Settings → Clear All Data. This removes everything permanently from your device.")
            ).forEach { (icon, color, pair) ->
                Box(modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassWhite)
                    .border(1.dp, color.copy(0.2f), RoundedCornerShape(16.dp))
                    .padding(14.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp).padding(top = 2.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(pair.first, style = MaterialTheme.typography.labelLarge,
                                color = color, fontWeight = FontWeight.Bold)
                            Text(pair.second, style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TIME PICKER DIALOG
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarkTimePickerDialog(
    initialHour: Int, initialMinute: Int,
    onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState(initialHour, initialMinute, is24Hour = false)
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = CardDark, shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(AmberGrad)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Schedule, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text("Reminder Time", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(GlassWhite).border(1.dp, GlassStroke, RoundedCornerShape(16.dp))
                .padding(8.dp), contentAlignment = Alignment.Center) {
                TimePicker(state = state, colors = TimePickerDefaults.colors(
                    clockDialColor                      = Color(0xFF111827),
                    clockDialSelectedContentColor       = Color.White,
                    clockDialUnselectedContentColor     = TextSecondary,
                    selectorColor                       = NeonViolet,
                    containerColor                      = Color.Transparent,
                    timeSelectorSelectedContainerColor  = NeonViolet.copy(0.2f),
                    timeSelectorUnselectedContainerColor= GlassWhite,
                    timeSelectorSelectedContentColor    = NeonViolet,
                    timeSelectorUnselectedContentColor  = TextSecondary
                ))
            }
        },
        confirmButton = {
            Box(modifier = Modifier.clip(RoundedCornerShape(12.dp))
                .background(Brush.horizontalGradient(VioletGrad))
                .clickable { onConfirm(state.hour, state.minute) }
                .padding(horizontal = 20.dp, vertical = 10.dp)) {
                Text("Set Time", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(GlassWhite)
                .border(1.dp, GlassStroke, RoundedCornerShape(12.dp))
                .clickable(onClick = onDismiss).padding(horizontal = 20.dp, vertical = 10.dp)) {
                Text("Cancel", color = TextSecondary, fontWeight = FontWeight.Medium)
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────────────────
fun formatTime(hour: Int, minute: Int): String {
    return try {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
        }
        java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(cal.time)
    } catch (e: Exception) {
        "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    }
}

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val notificationHour    : Int     = 9,
    val notificationMinute  : Int     = 0,
    val darkMode            : Boolean = true
)