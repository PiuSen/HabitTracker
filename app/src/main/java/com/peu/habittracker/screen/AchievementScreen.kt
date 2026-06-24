package com.peu.habittracker.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.peu.habittracker.db.Achievement
import com.peu.habittracker.viewModel.AchievementsViewModel
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
private val TextPrimary   = Color(0xFFF1F3FF)
private val TextSecondary = Color(0xFF8B90A8)
private val GlassWhite    = Color(0x14FFFFFF)
private val GlassStroke   = Color(0x26FFFFFF)

private val VioletGrad = listOf(NeonViolet, NeonCyan)
private val PinkGrad   = listOf(NeonPink, NeonViolet)
private val GreenGrad  = listOf(NeonGreen, Color(0xFF00B4DB))
private val AmberGrad  = listOf(NeonAmber, Color(0xFFFF6B00))
private val GoldGrad   = listOf(Color(0xFFFFD700), Color(0xFFFFB800))
private val SilverGrad = listOf(Color(0xFFE0E0E0), Color(0xFFBDBDBD))
private val BronzeGrad = listOf(Color(0xFFCD9B6B), Color(0xFFB07D4E))

// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AchievementsViewModel = hiltViewModel()
) {
    val achievements   by viewModel.achievements.collectAsState()
    val unlockedCount  by viewModel.unlockedCount.collectAsState()
    val listState      = rememberLazyListState()
    val isScrolled     by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    Box(modifier = Modifier.fillMaxSize().background(NightBase)) {
        AchievementsOrbs()

        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                // Top Bar
                AchievementsTopBar(
                    unlockedCount = unlockedCount,
                    total         = achievements.size,
                    onBack        = onNavigateBack,
                    isScrolled    = isScrolled
                )

                LazyColumn(
                    state           = listState,
                    modifier        = Modifier.fillMaxSize(),
                    contentPadding  = PaddingValues(
                        start  = 16.dp, end = 16.dp,
                        top    = 8.dp,  bottom = 40.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Hero progress card
                    item {
                        AchievementsHeroCard(
                            unlockedCount = unlockedCount,
                            total         = achievements.size
                        )
                    }

                    // Trophy showcase (top 3 unlocked)
                    val unlocked = achievements.filter { it.isUnlocked }
                    if (unlocked.isNotEmpty()) {
                        item {
                            AchievementsTrophyRow(unlocked = unlocked.take(3))
                        }
                    }

                    // Section: Unlocked
                    if (unlocked.isNotEmpty()) {
                        item {
                            AchievementsSectionLabel(
                                title    = "✅  Unlocked",
                                count    = unlocked.size,
                                gradient = GreenGrad
                            )
                        }
                        items(unlocked, key = { it.id }) { ach ->
                            AchievementCard(achievement = ach, isUnlocked = true)
                        }
                    }

                    // Section: Locked
                    val locked = achievements.filter { !it.isUnlocked }
                    if (locked.isNotEmpty()) {
                        item {
                            AchievementsSectionLabel(
                                title    = "🔒  Locked",
                                count    = locked.size,
                                gradient = listOf(TextSecondary, TextSecondary.copy(0.6f))
                            )
                        }
                        items(locked, key = { it.id }) { ach ->
                            AchievementCard(achievement = ach, isUnlocked = false)
                        }
                    }

                    if (achievements.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("🏆", style = MaterialTheme.typography.displayLarge)
                                    Text("No achievements yet",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary, fontWeight = FontWeight.Bold)
                                    Text("Complete habits to unlock trophies!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary)
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
private fun AchievementsOrbs() {
    val inf = rememberInfiniteTransition(label = "aorbs")
    val t by inf.animateFloat(0f, 1f,
        infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse), label = "at")
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(NeonAmber.copy(0.14f + t * 0.05f), Color.Transparent),
                radius = size.width * 0.5f, center = Offset(size.width * 0.85f, size.height * 0.08f)
            ), radius = size.width * 0.5f, center = Offset(size.width * 0.85f, size.height * 0.08f)
        )
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(NeonViolet.copy(0.08f), Color.Transparent),
                radius = size.width * 0.4f, center = Offset(0f, size.height * 0.4f)
            ), radius = size.width * 0.4f, center = Offset(0f, size.height * 0.4f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AchievementsTopBar(
    unlockedCount: Int, total: Int, onBack: () -> Unit, isScrolled: Boolean
) {
    val bgAlpha by animateFloatAsState(
        if (isScrolled) 0.9f else 0f, tween(300), label = "tba")
    Surface(color = NightBase.copy(alpha = bgAlpha), tonalElevation = 0.dp) {
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null,
                    tint = NeonAmber, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Achievements", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = (-0.5).sp)
                Text("$unlockedCount of $total unlocked",
                    style = MaterialTheme.typography.labelSmall, color = NeonAmber)
            }
            // Unlocked badge
            Surface(shape = RoundedCornerShape(50.dp), color = NeonAmber.copy(0.14f),
                modifier = Modifier.border(1.dp, NeonAmber.copy(0.4f), RoundedCornerShape(50.dp))) {
                Text("🏆 $unlockedCount",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonAmber, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HERO CARD  — ring progress
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AchievementsHeroCard(unlockedCount: Int, total: Int) {
    val progress = if (total > 0) unlockedCount.toFloat() / total else 0f
    val animProg by animateFloatAsState(progress,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "ap")
    val inf = rememberInfiniteTransition(label = "aring")
    val glow by inf.animateFloat(0.5f, 1f,
        infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "ag")

    Box(
        modifier = Modifier.fillMaxWidth().height(180.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(
                listOf(Color(0xFF1B1F3B), Color(0xFF0E1225)), Offset.Zero, Offset(1000f, 1000f)
            ))
            .border(1.dp, NeonAmber.copy(0.25f), RoundedCornerShape(28.dp))
    ) {
        // Dot grid
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 24.dp.toPx()
            var x = step / 2
            while (x < size.width) {
                var y = step / 2
                while (y < size.height) {
                    drawCircle(Color.White.copy(0.03f), 1.5f, Offset(x, y)); y += step
                }; x += step
            }
        }
        Row(
            modifier = Modifier.fillMaxSize().padding(22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Ring
            Box(Modifier.size(130.dp), contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(130.dp)) {
                    val stroke = 12.dp.toPx(); val inset = stroke / 2
                    val sz = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
                    drawArc(Color.White.copy(0.07f), -90f, 360f, false,
                        Offset(inset, inset), sz, style = Stroke(stroke, cap = StrokeCap.Round))
                    if (animProg > 0f) {
                        drawArc(Brush.sweepGradient(listOf(NeonAmber, Color(0xFFFF6B00), NeonAmber)),
                            -90f, animProg * 360f, false,
                            Offset(inset, inset), sz, style = Stroke(stroke, cap = StrokeCap.Round))
                        val angle = Math.toRadians((-90 + animProg * 360).toDouble())
                        val cx = size.width / 2 + cos(angle).toFloat() * (size.width / 2 - inset)
                        val cy = size.height / 2 + sin(angle).toFloat() * (size.height / 2 - inset)
                        drawCircle(NeonAmber.copy(glow), 7.dp.toPx(), Offset(cx, cy))
                        drawCircle(Color.White, 3.dp.toPx(), Offset(cx, cy))
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${(animProg * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black, color = TextPrimary)
                    Text("done", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Hall of\nFame", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black, color = TextPrimary,
                    letterSpacing = (-0.5).sp, lineHeight = 28.sp)
                Surface(shape = RoundedCornerShape(50.dp), color = NeonAmber.copy(0.14f),
                    modifier = Modifier.border(1.dp, NeonAmber.copy(0.4f), RoundedCornerShape(50.dp))) {
                    Text("$unlockedCount / $total trophies",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonAmber, fontWeight = FontWeight.Bold)
                }
                Text(
                    when {
                        progress >= 1f   -> "All trophies earned! 🎉"
                        progress >= 0.5f -> "More than halfway there!"
                        progress >  0f   -> "Keep going, warrior!"
                        else             -> "Start building habits!"
                    },
                    style = MaterialTheme.typography.bodySmall, color = TextSecondary
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TROPHY SHOWCASE ROW
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AchievementsTrophyRow(unlocked: List<Achievement>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("🏆  Recently Earned", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold, color = TextPrimary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val grads = listOf(GoldGrad, SilverGrad, BronzeGrad)
            unlocked.take(3).forEachIndexed { idx, ach ->
                val grad = grads.getOrElse(idx) { AmberGrad }
                Box(
                    modifier = Modifier.weight(1f).height(100.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(grad[0].copy(0.15f), grad[1].copy(0.05f))))
                        .border(1.dp, grad[0].copy(0.4f), RoundedCornerShape(20.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(ach.icon.ifBlank { "🏆" }, style = MaterialTheme.typography.headlineMedium)
                        Text(ach.title, style = MaterialTheme.typography.labelSmall,
                            color = grad[0], fontWeight = FontWeight.Bold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            // Fill empty slots if < 3
            repeat(3 - unlocked.size.coerceAtMost(3)) {
                Box(modifier = Modifier.weight(1f).height(100.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(GlassWhite)
                    .border(1.dp, GlassStroke, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("?", style = MaterialTheme.typography.headlineMedium, color = TextSecondary)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SECTION LABEL
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AchievementsSectionLabel(title: String, count: Int, gradient: List<Color>) {
    Row(
        modifier = Modifier.padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.width(3.dp).height(16.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Brush.verticalGradient(gradient)))
        Text(title, style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold, color = TextPrimary)
        Surface(shape = RoundedCornerShape(50.dp), color = GlassWhite,
            modifier = Modifier.border(1.dp, GlassStroke, RoundedCornerShape(50.dp))) {
            Text("$count", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ACHIEVEMENT CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AchievementCard(achievement: Achievement, isUnlocked: Boolean) {
    val scale by animateFloatAsState(
        if (isUnlocked) 1f else 0.98f,
        spring(Spring.DampingRatioMediumBouncy), label = "acs")

    // pick a gradient per achievement id for variety
    val accentGrad = when (achievement.id.toInt() % 4) {
        0    -> GoldGrad
        1    -> VioletGrad
        2    -> GreenGrad
        else -> PinkGrad
    }

    Box(
        modifier = Modifier.fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(22.dp))
            .background(if (isUnlocked) CardDark else CardDarker)
            .border(
                1.dp,
                if (isUnlocked) accentGrad[0].copy(0.4f) else GlassStroke,
                RoundedCornerShape(22.dp)
            )
    ) {
        // Left accent bar (unlocked only)
        if (isUnlocked) {
            Box(
                modifier = Modifier.width(3.dp).fillMaxHeight()
                    .background(Brush.verticalGradient(accentGrad),
                        RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp))
                    .align(Alignment.CenterStart)
            )
        }

        Row(
            modifier = Modifier
                .padding(
                    start  = if (isUnlocked) 20.dp else 16.dp,
                    end    = 16.dp, top = 16.dp, bottom = 16.dp
                )
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon orb
            Box(
                modifier = Modifier.size(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (isUnlocked)
                            Brush.linearGradient(listOf(accentGrad[0].copy(0.25f), accentGrad[1].copy(0.1f)))
                        else
                            Brush.linearGradient(listOf(GlassWhite, GlassWhite))
                    )
                    .border(
                        1.dp,
                        if (isUnlocked) accentGrad[0].copy(0.4f) else GlassStroke,
                        RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = if (isUnlocked) achievement.icon.ifBlank { "🏆" } else "🔒",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isUnlocked) Color.Unspecified else TextSecondary
                )
            }

            // Content
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(achievement.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) TextPrimary else TextSecondary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary, maxLines = 2)
                if (isUnlocked && achievement.unlockedAt != null) {
                    Surface(shape = RoundedCornerShape(50.dp),
                        color = accentGrad[0].copy(0.12f),
                        modifier = Modifier.border(1.dp, accentGrad[0].copy(0.3f), RoundedCornerShape(50.dp))) {
                        Text("🗓 ${formatDate(achievement.unlockedAt)}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = accentGrad[0], fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Status icon
            if (isUnlocked) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape)
                        .background(NeonGreen.copy(0.14f))
                        .border(1.dp, NeonGreen.copy(0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null,
                        tint = NeonGreen, modifier = Modifier.size(18.dp))
                }
            } else {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape)
                        .background(GlassWhite).border(1.dp, GlassStroke, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, null,
                        tint = TextSecondary.copy(0.5f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────────────────
fun formatDate(timestamp: Long): String {
    return try {
        val date      = java.util.Date(timestamp)
        val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) { "" }
}

private fun Modifier.graphicsLayer(block: androidx.compose.ui.graphics.layer.GraphicsLayer.() -> Unit) = this