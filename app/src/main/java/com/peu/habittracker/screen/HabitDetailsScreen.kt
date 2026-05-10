package com.peu.habittracker.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.peu.habittracker.db.HabitCompletion
import com.peu.habittracker.viewModel.HabitDetailViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// DESIGN TOKENS  (shared with HomeScreen / AddHabitScreen)
// ─────────────────────────────────────────────────────────────────────────────
private val NightBase     = Color(0xFF0A0D1A)
private val NightSurface  = Color(0xFF111827)
private val CardDark      = Color(0xFF1A2035)
private val CardDarker    = Color(0xFF141929)
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

// ─────────────────────────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    viewModel: HabitDetailViewModel = hiltViewModel()
) {
    val habit       by viewModel.habit.collectAsState(initial = null)
    val completions by viewModel.completions.collectAsState(initial = emptyList())
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(NightBase)) {
        DetailAmbientOrbs(accentColor = habit?.let { Color(it.color) } ?: NeonViolet)

        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // ── Top Bar ─────────────────────────────────────────────────
                DetailTopBar(
                    habitName   = habit?.name ?: "",
                    onBack      = onNavigateBack,
                    onEdit      = onNavigateToEdit,
                    onDelete    = { showDeleteDialog = true }
                )

                habit?.let { h ->
                    val habitColor  = Color(h.color)
                    val habitGrad   = listOf(habitColor, habitColor.copy(alpha = 0.6f))
                    val completionRate = if (completions.isNotEmpty())
                        completions.count { it.completed }.toFloat() / completions.size
                    else 0f

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(Modifier.height(4.dp))

                        // ── Hero Header Card ─────────────────────────────────
                        DetailHeroCard(h, habitColor, habitGrad)

                        // ── Radial Progress + 3 Stats ────────────────────────
                        DetailStatsSection(
                            currentStreak  = h.currentStreak,
                            longestStreak  = h.longestStreak,
                            totalDays      = h.totalCompletions,
                            completionRate = completionRate,
                            habitColor     = habitColor
                        )

                        // ── Heatmap Calendar ─────────────────────────────────
                        DetailHeatmapCard(completions = completions, habitColor = habitColor)

                        // ── Weekly Sparkline ─────────────────────────────────
                        DetailWeeklySparkline(completions = completions, habitColor = habitColor)

                        // ── Recent Activity ──────────────────────────────────
                        DetailRecentActivity(completions = completions, habitColor = habitColor)
                    }
                } ?: run {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = NeonViolet, modifier = Modifier.size(48.dp))
                    }
                }
            }
        }
    }

    // ── Delete Dialog ────────────────────────────────────────────────────────
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor   = CardDark,
            shape            = RoundedCornerShape(24.dp),
            title = {
                Text("Delete Habit?", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "This will permanently delete this habit and all its history. This cannot be undone.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(RedGrad))
                        .clickable {
                            showDeleteDialog = false
                            onNavigateBack()
                        }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassWhite)
                        .border(1.dp, GlassStroke, RoundedCornerShape(12.dp))
                        .clickable { showDeleteDialog = false }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text("Cancel", color = TextSecondary, fontWeight = FontWeight.Medium)
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AMBIENT ORBS  (tinted to habit color)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DetailAmbientOrbs(accentColor: Color) {
    val inf = rememberInfiniteTransition(label = "dorbs")
    val t by inf.animateFloat(
        0f, 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
        label = "dt"
    )
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(accentColor.copy(alpha = 0.15f + t * 0.05f), Color.Transparent),
                radius = size.width * 0.55f,
                center = Offset(size.width * 0.8f, size.height * 0.1f)
            ),
            radius = size.width * 0.55f,
            center = Offset(size.width * 0.8f, size.height * 0.1f)
        )
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(NeonViolet.copy(alpha = 0.08f), Color.Transparent),
                radius = size.width * 0.4f,
                center = Offset(0f, size.height * 0.45f)
            ),
            radius = size.width * 0.4f,
            center = Offset(0f, size.height * 0.45f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DetailTopBar(
    habitName: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(color = NightBase, tonalElevation = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back
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

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Habit Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp
                )
                if (habitName.isNotBlank()) {
                    Text(habitName, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }

            // Edit button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GlassWhite)
                    .border(1.dp, GlassStroke, CircleShape)
                    .clickable(onClick = onEdit),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Edit, null, tint = NeonViolet, modifier = Modifier.size(18.dp))
            }

            Spacer(Modifier.width(8.dp))

            // Delete button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NeonRed.copy(alpha = 0.12f))
                    .border(1.dp, NeonRed.copy(alpha = 0.3f), CircleShape)
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Delete, null, tint = NeonRed, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HERO HEADER CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DetailHeroCard(
    h: com.peu.habittracker.db.Habit,
    habitColor: Color,
    habitGrad: List<Color>
) {
    val inf = rememberInfiniteTransition(label = "hero")
    val pulse by inf.animateFloat(
        0.88f, 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "hp"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1B1F3B), Color(0xFF0E1225)),
                    Offset.Zero, Offset(1000f, 1000f)
                )
            )
            .border(1.dp, habitColor.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
    ) {
        // Dot grid texture
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 24.dp.toPx()
            var x = step / 2
            while (x < size.width) {
                var y = step / 2
                while (y < size.height) {
                    drawCircle(Color.White.copy(alpha = 0.03f), radius = 1.5f, center = Offset(x, y))
                    y += step
                }
                x += step
            }
        }
        // Soft color bleed from habit color
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush  = Brush.radialGradient(
                    listOf(habitColor.copy(alpha = 0.2f), Color.Transparent),
                    radius = size.height * 0.9f,
                    center = Offset(size.width * 0.2f, size.height * 0.5f)
                ),
                radius = size.height * 0.9f,
                center = Offset(size.width * 0.2f, size.height * 0.5f)
            )
        }

        Row(
            modifier = Modifier.fillMaxSize().padding(22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Pulsing icon orb
            Box(
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush  = Brush.radialGradient(
                            listOf(habitColor.copy(alpha = 0.28f * pulse), Color.Transparent)
                        ),
                        radius = size.minDimension / 2
                    )
                }
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .scale(pulse)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Brush.linearGradient(habitGrad))
                        .border(1.dp, habitColor.copy(alpha = 0.4f), RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(h.icon.ifBlank { "✨" }, style = MaterialTheme.typography.displaySmall)
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    h.name,
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color      = TextPrimary,
                    letterSpacing = (-0.5).sp
                )

                if (h.description.isNotBlank()) {
                    Text(
                        h.description,
                        style   = MaterialTheme.typography.bodySmall,
                        color   = TextSecondary,
                        maxLines = 2
                    )
                }

                // Streak pill
                Surface(
                    shape  = RoundedCornerShape(50.dp),
                    color  = habitColor.copy(alpha = 0.15f),
                    modifier = Modifier.border(1.dp, habitColor.copy(alpha = 0.4f), RoundedCornerShape(50.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.LocalFireDepartment, null,
                            tint = NeonAmber, modifier = Modifier.size(13.dp))
                        Text(
                            "${h.currentStreak} day streak",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STATS SECTION  — radial ring + 3 chips
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DetailStatsSection(
    currentStreak: Int,
    longestStreak: Int,
    totalDays: Int,
    completionRate: Float,
    habitColor: Color
) {
    val animRate by animateFloatAsState(
        completionRate,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "rate"
    )
    val inf = rememberInfiniteTransition(label = "ring2")
    val glow by inf.animateFloat(
        0.5f, 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "rg"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Radial completion ring
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(CardDark)
                .border(1.dp, habitColor.copy(alpha = 0.25f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(100.dp)) {
                val stroke = 10.dp.toPx()
                val inset  = stroke / 2
                drawArc(
                    color      = Color.White.copy(alpha = 0.08f),
                    startAngle = -90f, sweepAngle = 360f, useCenter = false,
                    topLeft    = Offset(inset, inset),
                    size       = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
                    style      = Stroke(stroke, cap = StrokeCap.Round)
                )
                drawArc(
                    brush      = Brush.sweepGradient(listOf(habitColor, habitColor.copy(alpha = 0.5f), habitColor)),
                    startAngle = -90f, sweepAngle = animRate * 360f, useCenter = false,
                    topLeft    = Offset(inset, inset),
                    size       = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
                    style      = Stroke(stroke, cap = StrokeCap.Round)
                )
                // Glow dot
                if (animRate > 0f) {
                    val angle = Math.toRadians((-90f + animRate * 360f).toDouble())
                    val cx = size.width  / 2 + cos(angle).toFloat() * (size.width  / 2 - inset)
                    val cy = size.height / 2 + sin(angle).toFloat() * (size.height / 2 - inset)
                    drawCircle(habitColor.copy(alpha = glow), radius = 7.dp.toPx(), center = Offset(cx, cy))
                    drawCircle(Color.White, radius = 3.dp.toPx(), center = Offset(cx, cy))
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${(animRate * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Text("Done", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }

        // 3 stat chips stacked
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailStatChip("🔥", "$currentStreak", "Current Streak", AmberGrad)
            DetailStatChip("⭐", "$longestStreak", "Best Streak",    PinkGrad)
            DetailStatChip("✅", "$totalDays",     "Total Done",     GreenGrad)
        }
    }
}

@Composable
private fun DetailStatChip(
    emoji: String,
    value: String,
    label: String,
    grad: List<Color>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .border(1.dp, grad[0].copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Top accent line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Brush.horizontalGradient(grad))
                .align(Alignment.TopCenter)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(emoji, style = MaterialTheme.typography.bodyLarge)
            Column {
                Text(value, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black, color = TextPrimary)
                Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HEATMAP CALENDAR
// ─────────────────────────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DetailHeatmapCard(
    completions: List<HabitCompletion>,
    habitColor: Color
) {
    val today = LocalDate.now()
    val startDate = today.minusWeeks(6).with(java.time.DayOfWeek.MONDAY)

    val doneSet = completions
        .filter { it.completed }
        .map { it.date }
        .toSet()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardDark)
            .border(1.dp, GlassStroke, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // ── Header ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activity Heatmap",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(GlassWhite)
                    )
                    Text("Miss", color = TextSecondary)

                    Spacer(Modifier.width(4.dp))

                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(habitColor.copy(alpha = 0.8f))
                    )
                    Text("Hit", color = TextSecondary)
                }
            }

            // ── Day labels ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su").forEach { d ->
                    Text(
                        text = d,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.width(34.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ── Grid ───────────────────────────────────────────────
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(240.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                userScrollEnabled = false
            ) {

                items(49) { idx ->

                    val date = startDate.plusDays(idx.toLong())
                    val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

                    val isDone = doneSet.contains(dateStr)
                    val isToday = date == today
                    val isFuture = date > today

                    val backgroundModifier = if (isFuture) {
                        Modifier.background(GlassWhite.copy(alpha = 0.03f))
                    } else if (isDone) {
                        Modifier.background(
                            brush = Brush.linearGradient(
                                listOf(
                                    habitColor,
                                    habitColor.copy(alpha = 0.65f)
                                )
                            )
                        )
                    } else {
                        Modifier.background(
                            brush = Brush.linearGradient(
                                listOf(GlassWhite, GlassWhite)
                            )
                        )
                    }

                    val borderModifier = if (isToday) {
                        Modifier.border(
                            2.dp,
                            NeonCyan.copy(alpha = 0.7f),
                            RoundedCornerShape(8.dp)
                        )
                    } else Modifier

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .then(backgroundModifier)
                            .then(borderModifier),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isDone -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color.White
                                )
                            }

                            isToday -> {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(NeonCyan)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WEEKLY SPARKLINE  — last 4 weeks bar chart
// ─────────────────────────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DetailWeeklySparkline(
    completions: List<HabitCompletion>,
    habitColor: Color
) {
    val today   = LocalDate.now()
    val doneSet = completions.filter { it.completed }.map { it.date }.toSet()

    // Build last 4 weeks (Mon–Sun) completion counts
    val weeks = (3 downTo 0).map { wk ->
        val monday = today.minusWeeks(wk.toLong()).with(java.time.DayOfWeek.MONDAY)
        val label  = if (wk == 0) "This\nWeek" else "${wk}w\nago"
        val count  = (0..6).count { d ->
            doneSet.contains(monday.plusDays(d.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE))
        }
        label to count
    }
    val maxCount = weeks.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardDark)
            .border(1.dp, GlassStroke, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Weekly Breakdown", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = TextPrimary)

            Row(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                weeks.forEach { (label, count) ->
                    val fraction = count.toFloat() / maxCount
                    val animH by animateFloatAsState(
                        fraction,
                        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
                        label = "bar"
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Count badge
                        if (count > 0) {
                            Text(
                                "$count",
                                style = MaterialTheme.typography.labelSmall,
                                color = habitColor,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(3.dp))
                        }
                        // Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(animH.coerceAtLeast(0.05f))
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    if (count > 0)
                                        Brush.verticalGradient(listOf(habitColor, habitColor.copy(alpha = 0.4f)))
                                    else
                                        Brush.verticalGradient(listOf(GlassWhite, GlassWhite))
                                )
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(label, style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RECENT ACTIVITY
// ─────────────────────────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DetailRecentActivity(
    completions: List<HabitCompletion>,
    habitColor: Color
) {
    val recent = completions.sortedByDescending { it.date }.take(7)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardDark)
            .border(1.dp, GlassStroke, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            // Header
            Row(
                Modifier.fillMaxWidth().padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Activity", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = TextPrimary)
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = GlassWhite,
                    modifier = Modifier.border(1.dp, GlassStroke, RoundedCornerShape(50.dp))
                ) {
                    Text(
                        "Last 7 days",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            if (recent.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), Alignment.Center) {
                    Text("No activity recorded yet", style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary)
                }
            } else {
                recent.forEachIndexed { idx, item ->
                    DetailActivityRow(item = item, habitColor = habitColor)
                    if (idx < recent.lastIndex) {
                        Box(
                            modifier = Modifier
                                .padding(start = 52.dp)
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(GlassStroke)
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DetailActivityRow(item: HabitCompletion, habitColor: Color) {
    val date      = LocalDate.parse(item.date)
    val formatter = DateTimeFormatter.ofPattern("EEE, MMM d")
    val isToday   = date == LocalDate.now()
    val isYesterday = date == LocalDate.now().minusDays(1)

    val dateLabel = when {
        isToday     -> "Today"
        isYesterday -> "Yesterday"
        else        -> date.format(formatter)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Status icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (item.completed) habitColor.copy(alpha = 0.15f)
                    else GlassWhite
                )
                .border(
                    1.dp,
                    if (item.completed) habitColor.copy(alpha = 0.4f) else GlassStroke,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (item.completed) Icons.Default.Check else Icons.Default.Close,
                null,
                tint = if (item.completed) habitColor else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(dateLabel, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold, color = TextPrimary)
            if (item.note.isNotBlank()) {
                Text(item.note, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }

        // Status chip
        Surface(
            shape = RoundedCornerShape(50.dp),
            color = if (item.completed) habitColor.copy(alpha = 0.12f) else GlassWhite,
            modifier = Modifier.border(
                1.dp,
                if (item.completed) habitColor.copy(alpha = 0.35f) else GlassStroke,
                RoundedCornerShape(50.dp)
            )
        ) {
            Text(
                if (item.completed) "✓ Done" else "✗ Missed",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = if (item.completed) habitColor else TextSecondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SCALE MODIFIER HELPER
// ─────────────────────────────────────────────────────────────────────────────
private fun Modifier.scale(scale: Float) = this.graphicsLayer { scaleX = scale; scaleY = scale }