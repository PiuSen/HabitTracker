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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.peu.habittracker.util.HabitAnalytics
import com.peu.habittracker.util.OverallStatistics
import com.peu.habittracker.util.Trend
import com.peu.habittracker.viewModel.AnalyticsUiState
import com.peu.habittracker.viewModel.AnalyticsViewModel
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// DESIGN TOKENS
// ─────────────────────────────────────────────────────────────────────────────
private val NightBase     = Color(0xFF0A0D1A)
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
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(NightBase)) {
        AnalyticsOrbs()

        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                AnalyticsTopBar(onBack = onNavigateBack)

                when (val state = uiState) {
                    is AnalyticsUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            CircularProgressIndicator(color = NeonViolet, modifier = Modifier.size(48.dp))
                        }
                    }

                    is AnalyticsUiState.Error -> {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(modifier = Modifier.size(72.dp).clip(CircleShape)
                                    .background(NeonRed.copy(0.12f))
                                    .border(1.dp, NeonRed.copy(0.3f), CircleShape),
                                    contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.ErrorOutline, null,
                                        tint = NeonRed, modifier = Modifier.size(36.dp))
                                }
                                Text(state.message, style = MaterialTheme.typography.bodyLarge,
                                    color = TextSecondary, textAlign = TextAlign.Center)
                            }
                        }
                    }

                    is AnalyticsUiState.Success -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 40.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Spacer(Modifier.height(4.dp))

                            // ── Overall stats hero ───────────────────────────
                            AnalyticsOverallCard(stats = state.overallStats)

                            // ── Weekly comparison ────────────────────────────
                            AnalyticsWeeklyComparison(
                                thisWeek = state.overallStats.thisWeekCompletions,
                                lastWeek = state.overallStats.lastWeekCompletions
                            )

                            // ── Per-habit section ────────────────────────────
                            if (state.habitAnalytics.isNotEmpty()) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    Arrangement.SpaceBetween, Alignment.CenterVertically
                                ) {
                                    Text("Habit Performance",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Surface(shape = RoundedCornerShape(50.dp), color = GlassWhite,
                                        modifier = Modifier.border(1.dp, GlassStroke, RoundedCornerShape(50.dp))) {
                                        Text("${state.habitAnalytics.size} habits",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    }
                                }

                                state.habitAnalytics.forEach { analytics ->
                                    AnalyticsHabitCard(analytics = analytics)
                                }
                            } else {
                                Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), Alignment.Center) {
                                    Text("No habit data available yet",
                                        color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AMBIENT ORBS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AnalyticsOrbs() {
    val inf = rememberInfiniteTransition(label = "anorbs")
    val t by inf.animateFloat(0f, 1f,
        infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Reverse), label = "ant")
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(NeonCyan.copy(0.13f + t * 0.05f), Color.Transparent),
                radius = size.width * 0.5f, center = Offset(size.width * 0.9f, size.height * 0.08f)
            ), radius = size.width * 0.5f, center = Offset(size.width * 0.9f, size.height * 0.08f)
        )
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(NeonViolet.copy(0.08f), Color.Transparent),
                radius = size.width * 0.4f, center = Offset(0f, size.height * 0.5f)
            ), radius = size.width * 0.4f, center = Offset(0f, size.height * 0.5f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AnalyticsTopBar(onBack: () -> Unit) {
    Surface(color = NightBase, tonalElevation = 0.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape)
                .background(GlassWhite).border(1.dp, GlassStroke, CircleShape)
                .clickable(onClick = onBack), contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null,
                    tint = NeonViolet, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Analytics", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = (-0.5).sp)
                Text("Deep dive into your habits", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Box(modifier = Modifier.size(40.dp).clip(CircleShape)
                .background(GlassWhite).border(1.dp, GlassStroke, CircleShape)
                .clickable {}, contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Share, null, tint = NeonViolet, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// OVERALL STATS CARD  — ring + 3 badges
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AnalyticsOverallCard(stats: OverallStatistics) {
    val rate     = stats.overallCompletionRate.coerceIn(0f, 1f)
    val animRate by animateFloatAsState(rate,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "oar")
    val inf = rememberInfiniteTransition(label = "oring")
    val glow by inf.animateFloat(0.5f, 1f,
        infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "og")

    Box(modifier = Modifier.fillMaxWidth().height(200.dp)
        .clip(RoundedCornerShape(28.dp))
        .background(Brush.linearGradient(
            listOf(Color(0xFF1B1F3B), Color(0xFF0E1225)), Offset.Zero, Offset(1000f, 1000f)
        ))
        .border(1.dp, GlassStroke, RoundedCornerShape(28.dp))
    ) {
        // grid lines
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 28.dp.toPx(); var x = 0f
            while (x < size.width) {
                drawLine(Color.White.copy(0.025f), Offset(x, 0f), Offset(x, size.height), 1f); x += step
            }
        }
        Row(modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)) {

            // Ring
            Box(Modifier.size(140.dp), contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(140.dp)) {
                    val stroke = 12.dp.toPx(); val inset = stroke / 2
                    val sz = Size(size.width - stroke, size.height - stroke)
                    drawArc(Color.White.copy(0.07f), -90f, 360f, false,
                        Offset(inset, inset), sz, style = Stroke(stroke, cap = StrokeCap.Round))
                    val rateGrad = when {
                        rate >= 0.8f -> listOf(NeonGreen, NeonCyan)
                        rate >= 0.5f -> listOf(NeonAmber, NeonCyan)
                        else         -> listOf(NeonRed, NeonPink)
                    }
                    if (animRate > 0f) {
                        drawArc(Brush.sweepGradient(rateGrad + rateGrad.reversed()),
                            -90f, animRate * 360f, false,
                            Offset(inset, inset), sz, style = Stroke(stroke, cap = StrokeCap.Round))
                        val angle = Math.toRadians((-90 + animRate * 360).toDouble())
                        val cx = size.width / 2 + cos(angle).toFloat() * (size.width / 2 - inset)
                        val cy = size.height / 2 + sin(angle).toFloat() * (size.height / 2 - inset)
                        drawCircle(rateGrad[0].copy(glow), 7.dp.toPx(), Offset(cx, cy))
                        drawCircle(Color.White, 3.dp.toPx(), Offset(cx, cy))
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${(animRate * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black, color = TextPrimary)
                    Text("overall", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }

            // Stats badges
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Overall\nProgress", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black, color = TextPrimary,
                    letterSpacing = (-0.5).sp, lineHeight = 24.sp)
                AnalyticsMiniChip("📋", "${stats.totalHabits}", "Habits", VioletGrad)
                AnalyticsMiniChip("🔥", "${stats.currentActiveStreaks}", "Streaks", AmberGrad)
                AnalyticsMiniChip("✅", "${stats.thisMonthCompletions}", "This month", GreenGrad)
            }
        }
    }
}

@Composable
private fun AnalyticsMiniChip(emoji: String, value: String, label: String, grad: List<Color>) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(grad[0].copy(0.12f))
            .border(1.dp, grad[0].copy(0.3f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(emoji, style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black, color = TextPrimary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WEEKLY COMPARISON CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AnalyticsWeeklyComparison(thisWeek: Int, lastWeek: Int) {
    val change      = thisWeek - lastWeek
    val isImproving = change >= 0
    val compGrad    = if (isImproving) GreenGrad else RedGrad
    val maxVal      = maxOf(thisWeek, lastWeek, 1)

    Box(modifier = Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(24.dp))
        .background(CardDark)
        .border(1.dp, compGrad[0].copy(0.25f), RoundedCornerShape(24.dp))
        .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(compGrad.map { it.copy(0.2f) }))
                        .border(1.dp, compGrad[0].copy(0.3f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, null,
                            tint = compGrad[0], modifier = Modifier.size(18.dp))
                    }
                    Text("Week vs Week", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Surface(shape = RoundedCornerShape(50.dp), color = compGrad[0].copy(0.12f),
                    modifier = Modifier.border(1.dp, compGrad[0].copy(0.35f), RoundedCornerShape(50.dp))) {
                    Text("${if (isImproving) "↑" else "↓"} ${abs(change)} completions",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = compGrad[0], fontWeight = FontWeight.Bold)
                }
            }

            // Visual bars comparison
            Row(Modifier.fillMaxWidth().height(100.dp),
                Arrangement.spacedBy(12.dp), Alignment.Bottom) {

                // This week bar
                val thisAnim by animateFloatAsState(thisWeek.toFloat() / maxVal,
                    spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "tw")
                Column(Modifier.weight(1f),  verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$thisWeek", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = if (isImproving) NeonGreen else NeonRed)
                    Spacer(Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(thisAnim.coerceAtLeast(0.05f))
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(Brush.verticalGradient(compGrad)))
                    Spacer(Modifier.height(8.dp))
                    Text("This Week", style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary, textAlign = TextAlign.Center)
                }

                // Last week bar
                val lastAnim by animateFloatAsState(lastWeek.toFloat() / maxVal,
                    spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "lw")
                Column(Modifier.weight(1f),  verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$lastWeek", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black, color = TextSecondary)
                    Spacer(Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(lastAnim.coerceAtLeast(0.05f))
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(Brush.verticalGradient(listOf(TextSecondary.copy(0.5f), GlassWhite))))
                    Spacer(Modifier.height(8.dp))
                    Text("Last Week", style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HABIT ANALYTICS CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AnalyticsHabitCard(analytics: HabitAnalytics) {
    val rate     = analytics.completionRate.coerceIn(0f, 1f)
    val animRate by animateFloatAsState(rate,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "har")

    val accentGrad = when {
        rate >= 0.8f -> GreenGrad
        rate >= 0.5f -> AmberGrad
        else         -> RedGrad
    }

    Box(modifier = Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(22.dp))
        .background(CardDark)
        .border(1.dp, accentGrad[0].copy(0.28f), RoundedCornerShape(22.dp))
    ) {
        // Left accent
        Box(modifier = Modifier.width(3.dp).fillMaxHeight()
            .background(Brush.verticalGradient(accentGrad),
                RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp))
            .align(Alignment.CenterStart))

        Column(modifier = Modifier.padding(start = 18.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // ── Header ──────────────────────────────────────────────────────
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(accentGrad[0].copy(0.2f), accentGrad[1].copy(0.1f))))
                        .border(1.dp, accentGrad[0].copy(0.3f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center) {
                        Text(analytics.habitIcon.ifBlank { "✨" },
                            style = MaterialTheme.typography.titleLarge)
                    }
                    Column {
                        Text(analytics.habitName, style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold, color = TextPrimary,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${analytics.completionRatePercentage}% completion",
                            style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }
                AnalyticsTrendBadge(trend = analytics.trend)
            }

            // ── Animated progress bar ───────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Completion Rate", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text("${(animRate * 100).toInt()}%", style = MaterialTheme.typography.labelSmall,
                        color = accentGrad[0], fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.fillMaxWidth().height(8.dp)
                    .clip(RoundedCornerShape(4.dp)).background(GlassWhite)) {
                    Box(modifier = Modifier.fillMaxWidth(animRate).fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Brush.horizontalGradient(accentGrad)))
                }
            }

            // ── Last 7 days dots ────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Last 7 Days", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
                    analytics.last7DaysProgress.forEach { completed ->
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (completed) Brush.linearGradient(accentGrad)
                                else Brush.linearGradient(listOf(GlassWhite, GlassWhite))
                            )
                            .border(1.dp, if (completed) accentGrad[0].copy(0.4f) else GlassStroke,
                                RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center) {
                            if (completed) {
                                Icon(Icons.Default.Check, null,
                                    tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }

            // ── 3 mini stats ────────────────────────────────────────────────
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                AnalyticsMiniStatChip("🔥", "${analytics.currentStreak}", "Streak", AmberGrad, Modifier.weight(1f))
                AnalyticsMiniStatChip("⭐", "${analytics.longestStreak}", "Best",   PinkGrad,  Modifier.weight(1f))
                AnalyticsMiniStatChip("✅", "${analytics.totalCompletions}", "Total", GreenGrad, Modifier.weight(1f))
            }

            // ── Best / Worst day ────────────────────────────────────────────
            Row(Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(GlassWhite)
                .border(1.dp, GlassStroke, RoundedCornerShape(12.dp))
                .padding(12.dp),
                Arrangement.SpaceEvenly, Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Best Day", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(analytics.bestDayOfWeek.ifBlank { "—" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold, color = NeonGreen)
                }
                Box(modifier = Modifier.width(1.dp).height(28.dp).background(GlassStroke))
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Worst Day", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(analytics.worstDayOfWeek.ifBlank { "—" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold, color = NeonRed)
                }
            }
        }
    }
}

@Composable
private fun AnalyticsMiniStatChip(
    emoji: String, value: String, label: String, grad: List<Color>, modifier: Modifier
) {
    Box(modifier = modifier.clip(RoundedCornerShape(12.dp))
        .background(CardDarker)
        .border(1.dp, grad[0].copy(0.25f), RoundedCornerShape(12.dp))
        .padding(vertical = 8.dp, horizontal = 10.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(2.dp)
            .background(Brush.horizontalGradient(grad)).align(Alignment.TopCenter))
        Column(modifier = Modifier.padding(top = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(emoji, style = MaterialTheme.typography.bodySmall)
            Text(value, style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black, color = TextPrimary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TREND BADGE
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AnalyticsTrendBadge(trend: Trend) {
    val (emoji, grad, text) = when (trend) {
        Trend.IMPROVING -> Triple("📈", GreenGrad, "Rising")
        Trend.STABLE    -> Triple("➡️", AmberGrad, "Stable")
        Trend.DECLINING -> Triple("📉", RedGrad,   "Falling")
    }
    Surface(shape = RoundedCornerShape(10.dp), color = grad[0].copy(0.12f),
        modifier = Modifier.border(1.dp, grad[0].copy(0.3f), RoundedCornerShape(10.dp))) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, style = MaterialTheme.typography.labelSmall)
            Text(text, style = MaterialTheme.typography.labelSmall,
                color = grad[0], fontWeight = FontWeight.Bold)
        }
    }
}