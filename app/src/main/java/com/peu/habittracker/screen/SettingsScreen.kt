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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.peu.habittracker.viewModel.SettingsViewModel

// ─────────────────────────────────────────────────────────────────────────────
// DESIGN TOKENS
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

// ─────────────────────────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val notificationTime     by viewModel.notificationTime.collectAsState()
    val darkMode             by viewModel.darkMode.collectAsState()
    var showTimePicker       by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(NightBase)) {
        SettingsAmbientOrbs()

        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // ── Top Bar ──────────────────────────────────────────────────
                SettingsTopBar(onBack = onNavigateBack)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(Modifier.height(4.dp))

                    // ── Profile / App branding card ──────────────────────────
                    SettingsBrandCard()

                    // ── Notifications ────────────────────────────────────────
                    SettingsGroupCard(title = "🔔  Notifications", gradient = VioletGrad) {
                        SettingsToggleRow(
                            icon        = Icons.Default.Notifications,
                            iconGrad    = VioletGrad,
                            title       = "Daily Reminders",
                            subtitle    = "Get nudged to complete your habits",
                            checked     = notificationsEnabled,
                            onToggle    = { viewModel.toggleNotifications(it) }
                        )
                        AnimatedVisibility(
                            visible = notificationsEnabled,
                            enter   = expandVertically() + fadeIn(),
                            exit    = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                SettingsDivider()
                                SettingsActionRow(
                                    icon     = Icons.Default.Schedule,
                                    iconGrad = AmberGrad,
                                    title    = "Reminder Time",
                                    subtitle = formatTime(notificationTime.first, notificationTime.second),
                                    badge    = formatTime(notificationTime.first, notificationTime.second),
                                    onClick  = { showTimePicker = true }
                                )
                            }
                        }
                    }

                    // ── Appearance ───────────────────────────────────────────
                    SettingsGroupCard(title = "🎨  Appearance", gradient = PinkGrad) {
                        SettingsToggleRow(
                            icon     = Icons.Default.DarkMode,
                            iconGrad = PinkGrad,
                            title    = "Dark Mode",
                            subtitle = "Always-on AMOLED dark theme",
                            checked  = darkMode,
                            onToggle = { viewModel.toggleDarkMode(it) }
                        )
                        SettingsDivider()
                        SettingsActionRow(
                            icon     = Icons.Default.Palette,
                            iconGrad = VioletGrad,
                            title    = "Accent Color",
                            subtitle = "Violet × Cyan",
                            onClick  = {}
                        )
                    }

                    // ── Data & Privacy ───────────────────────────────────────
                    SettingsGroupCard(title = "📦  Data & Privacy", gradient = GreenGrad) {
                        SettingsActionRow(
                            icon     = Icons.Default.Download,
                            iconGrad = GreenGrad,
                            title    = "Export Data",
                            subtitle = "Save habits as CSV or JSON",
                            onClick  = {}
                        )
                        SettingsDivider()
                        SettingsActionRow(
                            icon     = Icons.Default.Backup,
                            iconGrad = listOf(NeonCyan, NeonViolet),
                            title    = "Backup to Cloud",
                            subtitle = "Sync your progress",
                            onClick  = {}
                        )
                        SettingsDivider()
                        SettingsActionRow(
                            icon     = Icons.Default.DeleteForever,
                            iconGrad = listOf(NeonRed, NeonPink),
                            title    = "Clear All Data",
                            subtitle = "Reset the app completely",
                            onClick  = {},
                            isDanger = true
                        )
                    }

                    // ── About ────────────────────────────────────────────────
                    SettingsGroupCard(title = "ℹ️  About", gradient = AmberGrad) {
                        SettingsActionRow(
                            icon     = Icons.Default.Info,
                            iconGrad = AmberGrad,
                            title    = "Version",
                            subtitle = "1.0.0  (Build 1)",
                            onClick  = {}
                        )
                        SettingsDivider()
                        SettingsActionRow(
                            icon     = Icons.Default.Code,
                            iconGrad = VioletGrad,
                            title    = "Built with",
                            subtitle = "Jetpack Compose · Kotlin · MVVM",
                            onClick  = {}
                        )
                        SettingsDivider()
                        SettingsActionRow(
                            icon     = Icons.Default.Star,
                            iconGrad = listOf(NeonAmber, NeonPink),
                            title    = "Rate the App",
                            subtitle = "Love it? Leave us a review ⭐",
                            onClick  = {}
                        )
                    }
                }
            }
        }
    }

    // ── Time Picker Dialog ───────────────────────────────────────────────────
    if (showTimePicker) {
        DarkTimePickerDialog(
            initialHour   = notificationTime.first,
            initialMinute = notificationTime.second,
            onDismiss     = { showTimePicker = false },
            onConfirm     = { h, m ->
                viewModel.updateNotificationTime(h, m)
                showTimePicker = false
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AMBIENT ORBS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SettingsAmbientOrbs() {
    val inf = rememberInfiniteTransition(label = "sorbs")
    val t by inf.animateFloat(
        0f, 1f,
        animationSpec = infiniteRepeatable(tween(11000, easing = LinearEasing), RepeatMode.Reverse),
        label = "st"
    )
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(NeonViolet.copy(alpha = 0.13f + t * 0.05f), Color.Transparent),
                radius = size.width * 0.5f,
                center = Offset(size.width * 0.85f, size.height * 0.05f)
            ),
            radius = size.width * 0.5f,
            center = Offset(size.width * 0.85f, size.height * 0.05f)
        )
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(NeonPink.copy(alpha = 0.08f), Color.Transparent),
                radius = size.width * 0.35f,
                center = Offset(0f, size.height * 0.4f)
            ),
            radius = size.width * 0.35f,
            center = Offset(0f, size.height * 0.4f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    Surface(color = NightBase, tonalElevation = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GlassWhite)
                    .border(1.dp, GlassStroke, CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = NeonViolet, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp
                )
                Text("Customize your experience", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BRAND CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SettingsBrandCard() {
    val inf = rememberInfiniteTransition(label = "brand")
    val pulse by inf.animateFloat(
        0.9f, 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bp"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1B1F3B), Color(0xFF0E1225)),
                    Offset.Zero, Offset(1000f, 600f)
                )
            )
            .border(1.dp, GlassStroke, RoundedCornerShape(28.dp))
            .padding(22.dp)
    ) {
        // dot grid
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 24.dp.toPx()
            var x = step / 2
            while (x < size.width) {
                var y = step / 2
                while (y < size.height) {
                    drawCircle(Color.White.copy(alpha = 0.03f), 1.5f, center = Offset(x, y))
                    y += step
                }
                x += step
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // App icon orb
            Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush  = Brush.radialGradient(listOf(NeonViolet.copy(alpha = 0.3f * pulse), Color.Transparent)),
                        radius = size.minDimension / 2
                    )
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(VioletGrad)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎯", style = MaterialTheme.typography.titleLarge)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("HabitTracker", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = (-0.5).sp)
                Text("Build habits. Track progress. Win.",
                    style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = NeonViolet.copy(alpha = 0.15f),
                    modifier = Modifier.border(1.dp, NeonViolet.copy(alpha = 0.35f), RoundedCornerShape(50.dp))
                ) {
                    Text("v1.0.0  ✨ Premium",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonViolet, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GROUP CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SettingsGroupCard(
    title: String,
    gradient: List<Color>,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        // Section label
        Row(
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Brush.verticalGradient(gradient))
            )
            Text(title, style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CardDark)
                .border(1.dp, gradient[0].copy(alpha = 0.2f), RoundedCornerShape(20.dp))
        ) {
            Column { content() }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOGGLE ROW
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SettingsToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconGrad: List<Color>,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Icon box
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Brush.linearGradient(listOf(iconGrad[0].copy(alpha = 0.2f), iconGrad[1].copy(alpha = 0.1f))))
                .border(1.dp, iconGrad[0].copy(alpha = 0.3f), RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconGrad[0], modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        // Custom neon switch
        NeonSwitch(checked = checked, onToggle = onToggle, gradient = iconGrad)
    }
}

@Composable
private fun NeonSwitch(checked: Boolean, onToggle: (Boolean) -> Unit, gradient: List<Color>) {
    val thumbPos by animateFloatAsState(if (checked) 1f else 0f,
        spring(Spring.DampingRatioMediumBouncy), label = "sw")
    val trackColor by animateColorAsState(
        if (checked) gradient[0].copy(alpha = 0.3f) else GlassWhite, tween(200), label = "swc")
    val borderColor by animateColorAsState(
        if (checked) gradient[0].copy(alpha = 0.6f) else GlassStroke, tween(200), label = "sbc")

    Box(
        modifier = Modifier
            .width(48.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(trackColor)
            .border(1.dp, borderColor, RoundedCornerShape(13.dp))
            .clickable { onToggle(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = (3 + thumbPos * 22).dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    if (checked) Brush.linearGradient(gradient)
                    else Brush.linearGradient(listOf(TextSecondary.copy(0.5f), TextSecondary.copy(0.3f)))
                )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ACTION ROW
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SettingsActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconGrad: List<Color>,
    title: String,
    subtitle: String,
    badge: String? = null,
    onClick: () -> Unit,
    isDanger: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(
                    if (isDanger) Brush.linearGradient(listOf(NeonRed.copy(0.2f), NeonPink.copy(0.1f)))
                    else Brush.linearGradient(listOf(iconGrad[0].copy(0.2f), iconGrad[1].copy(0.1f)))
                )
                .border(1.dp, if (isDanger) NeonRed.copy(0.3f) else iconGrad[0].copy(0.3f), RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = if (isDanger) NeonRed else iconGrad[0], modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isDanger) NeonRed else TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        if (badge != null) {
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = iconGrad[0].copy(alpha = 0.12f),
                modifier = Modifier.border(1.dp, iconGrad[0].copy(0.3f), RoundedCornerShape(50.dp))
            ) {
                Text(badge,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = iconGrad[0], fontWeight = FontWeight.Bold)
            }
        } else {
            Icon(Icons.Default.ChevronRight, null,
                tint = if (isDanger) NeonRed.copy(0.5f) else TextSecondary,
                modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .padding(start = 68.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(GlassStroke)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// DARK TIME PICKER DIALOG
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarkTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState(initialHour, initialMinute, is24Hour = false)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = CardDark,
        shape            = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(AmberGrad)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Schedule, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text("Reminder Time", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassWhite)
                    .border(1.dp, GlassStroke, RoundedCornerShape(16.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(
                    state  = state,
                    colors = TimePickerDefaults.colors(
                        clockDialColor           = Color(0xFF111827),
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = TextSecondary,
                        selectorColor            = NeonViolet,
                        containerColor           = Color.Transparent,
                        timeSelectorSelectedContainerColor   = NeonViolet.copy(alpha = 0.2f),
                        timeSelectorUnselectedContainerColor = GlassWhite,
                        timeSelectorSelectedContentColor     = NeonViolet,
                        timeSelectorUnselectedContentColor   = TextSecondary
                    )
                )
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(VioletGrad))
                    .clickable { onConfirm(state.hour, state.minute) }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("Set Time", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(GlassWhite)
                    .border(1.dp, GlassStroke, RoundedCornerShape(12.dp))
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("Cancel", color = TextSecondary, fontWeight = FontWeight.Medium)
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────────────────
fun formatTime(hour: Int, minute: Int): String {
    val cal = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, hour)
        set(java.util.Calendar.MINUTE, minute)
    }
    return java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(cal.time)
}

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val notificationHour: Int = 9,
    val notificationMinute: Int = 0,
    val darkMode: Boolean = false
)