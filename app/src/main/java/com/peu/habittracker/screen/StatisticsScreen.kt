package com.peu.habittracker.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.peu.habittracker.viewModel.HomeUiState
import com.peu.habittracker.viewModel.HomeViewModel
import com.peu.habittracker.viewModel.HabitWithStatus
import kotlin.math.cos
import kotlin.math.sin

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
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState     by viewModel.uiState.collectAsState()
    val weeklyStats by viewModel.weeklyStats.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(NightBase)) {
        StatsAmbientOrbs()

        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {

                // ── Top Bar ──────────────────────────────────────────────────
                StatsTopBar(onBack = onNavigateBack)

                when (uiState) {
                    is HomeUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            CircularProgressIndicator(color = NeonViolet, modifier = Modifier.size(48.dp))
                        }
                    }
                    is HomeUiState.Success -> {
                        val habits        = (uiState as HomeUiState.Success).habits
                        val totalHabits   = habits.size
                        val completedToday= habits.count { it.isCompletedToday }
                        val totalStreaks   = habits.sumOf { it.habit.currentStreak }
                        val longestStreak = habits.maxOfOrNull { it.habit.longestStreak } ?: 0
                        val completionRate= if (totalHabits > 0) completedToday.toFloat() / totalHabits else 0f

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 40.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Spacer(Modifier.height(4.dp))

                            // ── Hero overview ring ───────────────────────────
                            StatsHeroCard(
                                completedToday = completedToday,
                                totalHabits    = totalHabits,
                                completionRate = completionRate
                            )

                            // ── 4 neon stat chips (2×2) ──────────────────────
                            StatsChipGrid(
                                totalHabits    = totalHabits,
                                completedToday = completedToday,
                                totalStreaks   = totalStreaks,
                                longestStreak  = longestStreak
                            )

                            // ── Weekly bar chart ─────────────────────────────
                            StatsWeeklyChart(weeklyStats = weeklyStats)

                            // ── Donut breakdown ──────────────────────────────
                            StatsDonutCard(
                                completed = completedToday,
                                total     = totalHabits
                            )

                            // ── Top habits leaderboard ───────────────────────
                            StatsLeaderboard(habits = habits)

                            // ── Motivational banner ──────────────────────────
                            StatsMotivationBanner(completionRate = completionRate)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AMBIENT ORBS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StatsAmbientOrbs() {
    val inf = rememberInfiniteTransition(label = "stsorbs")
    val t by inf.animateFloat(0f, 1f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Reverse),
        label = "stt")
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(NeonCyan.copy(alpha = 0.12f + t * 0.05f), Color.Transparent),
                radius = size.width * 0.5f, center = Offset(size.width * 0.9f, size.height * 0.1f)
            ), radius = size.width * 0.5f, center = Offset(size.width * 0.9f, size.height * 0.1f)
        )
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(NeonViolet.copy(alpha = 0.1f), Color.Transparent),
                radius = size.width * 0.4f, center = Offset(0f, size.height * 0.35f)
            ), radius = size.width * 0.4f, center = Offset(0f, size.height * 0.35f)
        )
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(NeonGreen.copy(alpha = 0.06f), Color.Transparent),
                radius = size.width * 0.3f, center = Offset(size.width * 0.5f, size.height * 0.85f)
            ), radius = size.width * 0.3f, center = Offset(size.width * 0.5f, size.height * 0.85f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StatsTopBar(onBack: () -> Unit) {
    Surface(color = NightBase, tonalElevation = 0.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape)
                    .background(GlassWhite).border(1.dp, GlassStroke, CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = NeonViolet, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Statistics", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = (-0.5).sp)
                Text("Your habit performance", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            // Share button
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape)
                    .background(GlassWhite).border(1.dp, GlassStroke, CircleShape)
                    .clickable {},
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Share, null, tint = NeonViolet, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HERO CARD  — large ring + summary
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StatsHeroCard(completedToday: Int, totalHabits: Int, completionRate: Float) {
    val animRate by animateFloatAsState(completionRate,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "hr")
    val inf = rememberInfiniteTransition(label = "hring")
    val glow by inf.animateFloat(0.5f, 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "hg")

    Box(
        modifier = Modifier.fillMaxWidth().height(190.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(
                listOf(Color(0xFF1B1F3B), Color(0xFF0E1225)), Offset.Zero, Offset(1000f, 1000f)
            ))
            .border(1.dp, GlassStroke, RoundedCornerShape(28.dp))
    ) {
        // grid
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 28.dp.toPx()
            var x = 0f
            while (x < size.width) {
                drawLine(Color.White.copy(0.025f), Offset(x, 0f), Offset(x, size.height), 1f)
                x += step
            }
        }
        Row(
            modifier = Modifier.fillMaxSize().padding(22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Ring
            Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(140.dp)) {
                    val stroke = 13.dp.toPx(); val inset = stroke / 2
                    drawArc(Color.White.copy(0.07f), -90f, 360f, false,
                        Offset(inset, inset), Size(size.width - stroke, size.height - stroke),
                        style = Stroke(stroke, cap = StrokeCap.Round))
                    drawArc(Brush.sweepGradient(listOf(NeonViolet, NeonCyan, NeonViolet)),
                        -90f, animRate * 360f, false,
                        Offset(inset, inset), Size(size.width - stroke, size.height - stroke),
                        style = Stroke(stroke, cap = StrokeCap.Round))
                    if (animRate > 0f) {
                        val angle = Math.toRadians((-90 + animRate * 360).toDouble())
                        val cx = size.width / 2 + cos(angle).toFloat() * (size.width / 2 - inset)
                        val cy = size.height / 2 + sin(angle).toFloat() * (size.height / 2 - inset)
                        drawCircle(NeonCyan.copy(glow), 8.dp.toPx(), Offset(cx, cy))
                        drawCircle(Color.White, 3.5.dp.toPx(), Offset(cx, cy))
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${(animRate * 100).toInt()}%",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black, color = TextPrimary)
                    Text("Today", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Overview", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = (-0.5).sp)
                Surface(shape = RoundedCornerShape(50.dp), color = NeonViolet.copy(0.15f),
                    modifier = Modifier.border(1.dp, NeonViolet.copy(0.4f), RoundedCornerShape(50.dp))) {
                    Text("$completedToday / $totalHabits done",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium, color = NeonViolet, fontWeight = FontWeight.Bold)
                }
                Text(getMotivationalText(completionRate),
                    style = MaterialTheme.typography.bodySmall, color = TextSecondary, lineHeight = 18.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4 STAT CHIPS  (2×2 grid)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StatsChipGrid(
    totalHabits: Int, completedToday: Int, totalStreaks: Int, longestStreak: Int
) {
    val items = listOf(
        Triple("📋", "$totalHabits",    "Total Habits")  to VioletGrad,
        Triple("✅", "$completedToday", "Done Today")    to GreenGrad,
        Triple("🔥", "$totalStreaks",   "Total Streaks") to AmberGrad,
        Triple("⭐", "$longestStreak",  "Best Streak")   to PinkGrad,
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (triple, grad) ->
                    val (emoji, value, label) = triple
                    StatsNeonChip(emoji, value, label, grad, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatsNeonChip(
    emoji: String, value: String, label: String, grad: List<Color>, modifier: Modifier
) {
    val animVal by animateFloatAsState(value.toFloatOrNull() ?: 0f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "cv")
    Box(
        modifier = modifier.height(95.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CardDark)
            .border(1.dp, grad[0].copy(0.28f), RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(2.dp)
            .background(Brush.horizontalGradient(grad)).align(Alignment.TopCenter))
        Column(modifier = Modifier.fillMaxSize().padding(top = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween) {
            Text(emoji, style = MaterialTheme.typography.titleMedium)
            Column {
                Text(animVal.toInt().toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black, color = TextPrimary)
                Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WEEKLY BAR CHART
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StatsWeeklyChart(weeklyStats: List<Int>) {
    val days   = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val maxVal = weeklyStats.maxOrNull()?.coerceAtLeast(1) ?: 1
    val bestIdx= weeklyStats.indexOf(weeklyStats.maxOrNull())

    Box(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardDark)
            .border(1.dp, GlassStroke, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Weekly Overview", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = TextPrimary)
                if (bestIdx >= 0) {
                    Surface(shape = RoundedCornerShape(50.dp), color = NeonAmber.copy(0.12f),
                        modifier = Modifier.border(1.dp, NeonAmber.copy(0.35f), RoundedCornerShape(50.dp))) {
                        Text("🔥 Best: ${days.getOrElse(bestIdx) { "" }}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonAmber, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyStats.forEachIndexed { idx, v ->
                    val frac = v.toFloat() / maxVal
                    val animH by animateFloatAsState(frac,
                        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "wh$idx")
                    val isBest = idx == bestIdx
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.Bottom,Alignment.CenterHorizontally, ) {
                        if (v > 0) {
                            Text("$v", style = MaterialTheme.typography.labelSmall,
                                color = if (isBest) NeonAmber else NeonViolet, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(3.dp))
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .fillMaxHeight(animH.coerceAtLeast(0.04f))
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    if (isBest) Brush.verticalGradient(AmberGrad)
                                    else Brush.verticalGradient(VioletGrad.map { it.copy(0.7f) })
                                )
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(days.getOrElse(idx) { "" },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isBest) NeonAmber else TextSecondary,
                            fontWeight = if (isBest) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DONUT BREAKDOWN CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StatsDonutCard(completed: Int, total: Int) {
    val pending = total - completed
    val rate    = if (total > 0) completed.toFloat() / total else 0f
    val animRate by animateFloatAsState(rate,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "dr")
    val inf = rememberInfiniteTransition(label = "donut")
    val glow by inf.animateFloat(0.5f, 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dg")

    Box(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardDark)
            .border(1.dp, GlassStroke, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Today's Breakdown", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = TextPrimary)

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                // Donut
                Box(Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(120.dp)) {
                        val stroke = 16.dp.toPx(); val inset = stroke / 2
                        val sz = Size(size.width - stroke, size.height - stroke)
                        // Pending arc
                        drawArc(Brush.sweepGradient(listOf(GlassWhite, GlassWhite.copy(0.5f))),
                            -90f + animRate * 360f, (1f - animRate) * 360f, false,
                            Offset(inset, inset), sz, style = Stroke(stroke, cap = StrokeCap.Butt))
                        // Completed arc
                        if (animRate > 0f) {
                            drawArc(Brush.sweepGradient(listOf(NeonViolet, NeonCyan, NeonViolet)),
                                -90f, animRate * 360f, false,
                                Offset(inset, inset), sz, style = Stroke(stroke, cap = StrokeCap.Round))
                            val angle = Math.toRadians((-90 + animRate * 360f).toDouble())
                            val cx = size.width / 2 + cos(angle).toFloat() * (size.width / 2 - inset)
                            val cy = size.height / 2 + sin(angle).toFloat() * (size.height / 2 - inset)
                            drawCircle(NeonCyan.copy(glow * 0.8f), 6.dp.toPx(), Offset(cx, cy))
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${(animRate * 100).toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black, color = TextPrimary)
                        Text("done", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }

                // Legend
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DonutLegendItem("Completed", "$completed", VioletGrad)
                    DonutLegendItem("Pending",   "$pending",   listOf(GlassWhite, GlassWhite.copy(0.5f)))
                    DonutLegendItem("Total",     "$total",     listOf(TextSecondary, TextSecondary))
                }
            }
        }
    }
}

@Composable
private fun DonutLegendItem(label: String, value: String, grad: List<Color>) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape)
            .background(Brush.linearGradient(grad)))
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.width(70.dp))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP HABITS LEADERBOARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StatsLeaderboard(habits: List<HabitWithStatus>) {
    val top = habits.sortedByDescending { it.habit.currentStreak }.take(3)

    Box(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardDark)
            .border(1.dp, GlassStroke, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Row(Modifier.fillMaxWidth().padding(bottom = 14.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.EmojiEvents, null, tint = NeonAmber, modifier = Modifier.size(20.dp))
                    Text("Top Performers", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Surface(shape = RoundedCornerShape(50.dp), color = NeonAmber.copy(0.12f),
                    modifier = Modifier.border(1.dp, NeonAmber.copy(0.35f), RoundedCornerShape(50.dp))) {
                    Text("Streak rank",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall, color = NeonAmber)
                }
            }

            if (top.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), Alignment.Center) {
                    Text("Start habits to see rankings", color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                top.forEachIndexed { idx, hws ->
                    LeaderboardRow(rank = idx + 1, hws = hws)
                    if (idx < top.lastIndex) {
                        Box(Modifier.padding(start = 60.dp).fillMaxWidth()
                            .height(1.dp).background(GlassStroke))
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(rank: Int, hws: HabitWithStatus) {
    val rankGrad = when (rank) {
        1 -> listOf(Color(0xFFFFD700), Color(0xFFFFB800))
        2 -> listOf(Color(0xFFBCC0CC), Color(0xFF8890A4))
        else -> listOf(Color(0xFFCD9B6B), Color(0xFFB07D4E))
    }
    val rankEmoji = when (rank) { 1 -> "🥇"; 2 -> "🥈"; else -> "🥉" }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Rank
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                .background(Brush.linearGradient(rankGrad.map { it.copy(0.2f) }))
                .border(1.dp, rankGrad[0].copy(0.4f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) { Text(rankEmoji, style = MaterialTheme.typography.bodyMedium) }

        // Icon
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(
                    Color(hws.habit.color).copy(0.2f), Color(hws.habit.color).copy(0.1f)
                )))
                .border(1.dp, Color(hws.habit.color).copy(0.3f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) { Text(hws.habit.icon.ifBlank { "✨" }, style = MaterialTheme.typography.titleMedium) }

        Column(modifier = Modifier.weight(1f)) {
            Text(hws.habit.name, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold, color = TextPrimary,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${hws.habit.totalCompletions} total completions",
                style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }

        Surface(shape = RoundedCornerShape(50.dp), color = NeonAmber.copy(0.12f),
            modifier = Modifier.border(1.dp, NeonAmber.copy(0.35f), RoundedCornerShape(50.dp))) {
            Text("🔥 ${hws.habit.currentStreak}d",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall, color = NeonAmber, fontWeight = FontWeight.Bold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MOTIVATIONAL BANNER
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StatsMotivationBanner(completionRate: Float) {
    val (emoji, message, grad) = when {
        completionRate >= 0.8f -> Triple("🎉", "Outstanding! You're absolutely crushing it today!", GreenGrad)
        completionRate >= 0.5f -> Triple("💪", "Great progress! Keep up the momentum!", VioletGrad)
        completionRate >  0f   -> Triple("🌱", "Good start! Every single step counts.", AmberGrad)
        else                   -> Triple("🎯", "Ready to build some great habits today?", PinkGrad)
    }

    Box(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(grad[0].copy(0.12f), grad[1].copy(0.06f))))
            .border(1.dp, grad[0].copy(0.3f), RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(emoji, style = MaterialTheme.typography.displaySmall)
            Text(message, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold, color = TextPrimary, lineHeight = 22.sp)
        }
    }
}