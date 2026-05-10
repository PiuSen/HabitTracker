package com.peu.habittracker.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.peu.habittracker.ui.theme.*
import com.peu.habittracker.viewModel.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// DESIGN TOKENS  (override in Theme.kt if you prefer)
// ─────────────────────────────────────────────────────────────────────────────
private val NightBase       = Color(0xFF0A0D1A)
private val NightSurface    = Color(0xFF111827)
private val CardDark        = Color(0xFF1A2035)
private val CardDarker      = Color(0xFF141929)
private val NeonViolet      = Color(0xFF7C6FFF)
private val NeonCyan        = Color(0xFF00D4FF)
private val NeonPink        = Color(0xFFFF6FD8)
private val NeonGreen       = Color(0xFF00F5A0)
private val NeonAmber       = Color(0xFFFFB800)
private val TextPrimary     = Color(0xFFF1F3FF)
private val TextSecondary   = Color(0xFF8B90A8)
private val GlassWhite      = Color(0x14FFFFFF)
private val GlassStroke     = Color(0x26FFFFFF)

private val VioletGrad  = listOf(NeonViolet, NeonCyan)
private val PinkGrad    = listOf(NeonPink, NeonViolet)
private val GreenGrad   = listOf(NeonGreen, Color(0xFF00B4DB))
private val AmberGrad   = listOf(NeonAmber, Color(0xFFFF6B00))

// ─────────────────────────────────────────────────────────────────────────────
enum class HabitFilter { ALL, COMPLETED, PENDING, STREAK }

// ─────────────────────────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddHabit: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToAchievement: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val deletedHabit by viewModel.deletedHabit.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var filter by remember { mutableStateOf(HabitFilter.ALL) }
    val listState = rememberLazyListState()

    val isScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    LaunchedEffect(deletedHabit) {
        deletedHabit?.let { habit ->
            val result = snackbarHostState.showSnackbar(
                message = "🗑️ ${habit.name} deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NightBase)
    ) {

        AmbientOrbs()

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) {
                    DarkSnackbar(it)
                }
            },
            floatingActionButton = {
                NeonFAB(onClick = onNavigateToAddHabit)
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                DarkHeader(
                    uiState = uiState,
                    isScrolled = isScrolled,
                    onStatistics = onNavigateToStatistics,
                    onSettings = onNavigateToSettings,
                    onAnalytics = onNavigateToAnalytics,
                    onAchievement = onNavigateToAchievement
                )

                if (uiState is HomeUiState.Success) {
                    val habits = (uiState as HomeUiState.Success).habits

                    NeonFilterPills(
                        selected = filter,
                        onChange = { filter = it },
                        habits = habits
                    )
                }

                when (val state = uiState) {

                    is HomeUiState.Loading -> {
                        DarkLoadingView()
                    }

                    is HomeUiState.Error -> {
                        DarkErrorView(state.message)
                    }

                    is HomeUiState.Success -> {

                        val filtered = when (filter) {
                            HabitFilter.ALL -> state.habits
                            HabitFilter.COMPLETED -> state.habits.filter { it.isCompletedToday }
                            HabitFilter.PENDING -> state.habits.filter { !it.isCompletedToday }
                            HabitFilter.STREAK -> state.habits.sortedByDescending { it.habit.currentStreak }
                        }

                        if (filtered.isEmpty()) {
                            DarkEmptyState(filter)
                        } else {

                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 4.dp,
                                    bottom = 120.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {

                                item {
                                    NeonRingProgressCard(habits = state.habits)
                                }

                                item {
                                    MiniStatsRow(habits = state.habits)
                                }

                                item {
                                    StreakLeaderboard(habits = state.habits)
                                }

                                items(
                                    items = filtered,
                                    key = { it.habit.id }
                                ) { hws ->

                                    GlassHabitCard(
                                        habitWithStatus = hws,
                                        onToggle = {
                                            viewModel.toggleHabitCompletion(it)
                                        },
                                        onClick = {
                                            onNavigateToDetail(it)
                                        },
//                                        modifier = Modifier.animateItem(
//                                            placementSpec = spring(
//                                                dampingRatio = Spring.DampingRatioMediumBouncy,
//                                                stiffness = Spring.StiffnessMedium
//                                            )
//                                        )
                                    )
                                }

                                item {
                                    DarkMotivationalFooter(
                                        rate =
                                            if (state.habits.isNotEmpty())
                                                state.habits.count { it.isCompletedToday }.toFloat() /
                                                        state.habits.size
                                            else 0f
                                    )
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
// AMBIENT ORB BACKGROUND
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AmbientOrbs() {
    val inf = rememberInfiniteTransition(label = "orbs")
    val t by inf.animateFloat(
        0f, 1f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Reverse),
        label = "t"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(NeonViolet.copy(alpha = 0.18f + t * 0.07f), Color.Transparent),
                radius = size.width * 0.55f,
                center = Offset(size.width * 0.15f, size.height * 0.12f)
            ),
            radius = size.width * 0.55f,
            center = Offset(size.width * 0.15f, size.height * 0.12f)
        )
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(NeonCyan.copy(alpha = 0.10f + t * 0.05f), Color.Transparent),
                radius = size.width * 0.45f,
                center = Offset(size.width * 0.85f, size.height * 0.35f)
            ),
            radius = size.width * 0.45f,
            center = Offset(size.width * 0.85f, size.height * 0.35f)
        )
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(NeonPink.copy(alpha = 0.08f), Color.Transparent),
                radius = size.width * 0.4f,
                center = Offset(size.width * 0.5f, size.height * 0.8f)
            ),
            radius = size.width * 0.4f,
            center = Offset(size.width * 0.5f, size.height * 0.8f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DARK HEADER
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun DarkHeader(
    uiState: HomeUiState,
    isScrolled: Boolean,
    onStatistics: () -> Unit,
    onSettings: () -> Unit,
    onAnalytics: () -> Unit,
    onAchievement: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val bgAlpha by animateFloatAsState(
        if (isScrolled) 0.85f else 0f, tween(300), label = "hdr"
    )

    Surface(
        color = NightSurface.copy(alpha = bgAlpha),
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Greeting badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GlassWhite,
                    modifier = Modifier
                        .border(1.dp, GlassStroke, RoundedCornerShape(20.dp))
                ) {
                    Text(
                        getGreeting(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonViolet,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "My Habits",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp
                )
                // Subtle date line
                Text(
                    LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassIconButton(onClick = onStatistics, icon = Icons.Default.BarChart)
                Box {
                    GlassIconButton(onClick = { showMenu = !showMenu }, icon = Icons.Default.MoreVert)
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = CardDark
                    ) {
                        DarkMenuOption(Icons.Default.Analytics,   "Analytics",    onAnalytics)   { showMenu = false }
                        DarkMenuOption(Icons.Default.EmojiEvents, "Achievements", onAchievement) { showMenu = false }
                        DarkMenuOption(Icons.Default.Download,    "Export Data",  {})            { showMenu = false }
                        DarkMenuOption(Icons.Default.Settings,    "Settings",     onSettings)    { showMenu = false }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(GlassWhite)
            .border(1.dp, GlassStroke, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun DarkMenuOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text, color = TextPrimary, fontWeight = FontWeight.Medium) },
        leadingIcon = { Icon(icon, null, tint = NeonViolet) },
        onClick = { onDismiss(); onClick() }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// NEON RING PROGRESS CARD  ── the hero section
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun NeonRingProgressCard(habits: List<HabitWithStatus>) {
    if (habits.isEmpty()) return

    val completed  = habits.count { it.isCompletedToday }
    val total      = habits.size
    val progress   = completed.toFloat() / total

    val animProg by animateFloatAsState(
        targetValue  = progress,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "prog"
    )

    val inf = rememberInfiniteTransition(label = "ring")
    val glow by inf.animateFloat(
        0.5f, 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1B1F3B), Color(0xFF0E1225)),
                    Offset(0f, 0f), Offset(1000f, 1000f)
                )
            )
            .border(1.dp, GlassStroke, RoundedCornerShape(28.dp))
    ) {
        // Background grid lines (subtle)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 32.dp.toPx()
            var x = 0f
            while (x < size.width) {
                drawLine(Color.White.copy(alpha = 0.03f), Offset(x, 0f), Offset(x, size.height), 1f)
                x += step
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ring
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(140.dp)) {
                    val stroke = 14.dp.toPx()
                    val inset  = stroke / 2
                    // Track
                    drawArc(
                        color       = Color.White.copy(alpha = 0.08f),
                        startAngle  = -90f,
                        sweepAngle  = 360f,
                        useCenter   = false,
                        topLeft     = Offset(inset, inset),
                        size        = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
                        style       = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    // Progress arc with gradient
                    drawArc(
                        brush       = Brush.sweepGradient(listOf(NeonViolet, NeonCyan, NeonViolet)),
                        startAngle  = -90f,
                        sweepAngle  = animProg * 360f,
                        useCenter   = false,
                        topLeft     = Offset(inset, inset),
                        size        = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
                        style       = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    // Glow dot at arc end
                    if (animProg > 0f) {
                        val angle  = Math.toRadians((-90f + animProg * 360f).toDouble())
                        val cx     = size.width  / 2 + cos(angle).toFloat() * (size.width  / 2 - inset)
                        val cy     = size.height / 2 + sin(angle).toFloat() * (size.height / 2 - inset)
                        drawCircle(NeonCyan.copy(alpha = glow), radius = 9.dp.toPx(), center = Offset(cx, cy))
                        drawCircle(Color.White, radius = 4.dp.toPx(), center = Offset(cx, cy))
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$completed",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        "of $total",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(Modifier.width(20.dp))

            // Right side info
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Today's\nProgress",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    lineHeight = 28.sp
                )

                // Pill badge
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = NeonViolet.copy(alpha = 0.18f),
                    modifier = Modifier.border(1.dp, NeonViolet.copy(alpha = 0.4f), RoundedCornerShape(50.dp))
                ) {
                    Text(
                        "${(animProg * 100).toInt()}% done",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonViolet,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    getMotivationalText(progress),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MINI STATS ROW
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MiniStatsRow(habits: List<HabitWithStatus>) {
    val totalStreaks     = habits.sumOf { it.habit.currentStreak }
    val longestStreak   = habits.maxOfOrNull { it.habit.longestStreak } ?: 0
    val totalCompletions= habits.sumOf { it.habit.totalCompletions }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        NeonStatChip("🔥", totalStreaks.toString(), "Active",  AmberGrad, Modifier.weight(1f))
        NeonStatChip("⭐", longestStreak.toString(), "Best",   PinkGrad,  Modifier.weight(1f))
        NeonStatChip("✅", totalCompletions.toString(),"Total", GreenGrad, Modifier.weight(1f))
    }
}

@Composable
private fun NeonStatChip(
    emoji: String,
    value: String,
    label: String,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(95.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CardDark)
            .border(1.dp, gradient[0].copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        // Top neon line accent
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Brush.horizontalGradient(gradient))
                .align(Alignment.TopCenter)
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(emoji, style = MaterialTheme.typography.titleMedium)
            Column {
                Text(
                    value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STREAK LEADERBOARD TEASER  (horizontal scroll of top streaks)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun StreakLeaderboard(habits: List<HabitWithStatus>) {
    val top = habits.filter { it.habit.currentStreak > 0 }
        .sortedByDescending { it.habit.currentStreak }
        .take(5)
    if (top.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "🔥 Hot Streaks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                "See all",
                style = MaterialTheme.typography.labelMedium,
                color = NeonViolet,
                fontWeight = FontWeight.SemiBold
            )
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(top) { hws ->
                StreakBadge(hws)
            }
        }
    }
}

@Composable
private fun StreakBadge(hws: HabitWithStatus) {
    val gradient = when ((hws.habit.currentStreak % 3)) {
        0    -> AmberGrad
        1    -> PinkGrad
        else -> GreenGrad
    }

    Box(
        modifier = Modifier
            .width(100.dp)
            .height(110.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(gradient[0].copy(alpha = 0.15f), gradient[1].copy(alpha = 0.05f))
                )
            )
            .border(1.dp, gradient[0].copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(hws.habit.icon.ifBlank { "✨" }, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(6.dp))
            Text(
                hws.habit.name,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = gradient[0].copy(alpha = 0.2f)
            ) {
                Text(
                    "${hws.habit.currentStreak}d 🔥",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = gradient[0],
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GLASS HABIT CARD
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlassHabitCard(
    habitWithStatus: HabitWithStatus,
    onToggle: (Long) -> Unit,
    onClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val habit       = habitWithStatus.habit
    val habitId     = habit.id
    val isCompleted = habitWithStatus.isCompletedToday

    val scale by animateFloatAsState(
        targetValue  = if (isCompleted) 0.98f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val borderColor by animateColorAsState(
        targetValue  = if (isCompleted) NeonViolet.copy(alpha = 0.5f) else GlassStroke,
        animationSpec = tween(400),
        label = "border"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(24.dp))
            .background(if (isCompleted) CardDarker else CardDark)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable { onClick(habitId) }
    ) {
        // Completed shimmer accent strip
        if (isCompleted) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(
                        Brush.verticalGradient(VioletGrad),
                        RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                    )
                    .align(Alignment.CenterStart)
            )
        }

        Row(
            modifier = Modifier
                .padding(start = if (isCompleted) 20.dp else 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon box
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isCompleted)
                            Brush.linearGradient(VioletGrad)
                        else
                            Brush.linearGradient(listOf(GlassWhite, GlassWhite))
                    )
                    .border(1.dp, if (isCompleted) NeonViolet.copy(alpha = 0.4f) else GlassStroke, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(habit.icon.ifBlank { "✨" }, style = MaterialTheme.typography.titleLarge)
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    habit.name,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) TextPrimary.copy(alpha = 0.7f) else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (isCompleted)
                        androidx.compose.ui.text.style.TextDecoration.LineThrough
                    else null
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        null,
                        tint = if (habit.currentStreak > 0) NeonAmber else TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        if (habit.currentStreak > 0) "${habit.currentStreak} day streak" else "No streak yet",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (habit.currentStreak > 0) NeonAmber else TextSecondary
                    )

                    // Best streak badge
                    if (habit.longestStreak >= 7) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = NeonPink.copy(alpha = 0.15f),
                            modifier = Modifier.border(1.dp, NeonPink.copy(alpha = 0.4f), RoundedCornerShape(50.dp))
                        ) {
                            Text(
                                "🏆 ${habit.longestStreak}d best",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonPink,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            // Toggle button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onToggle(habitId) })
                    }
            ) {
                val sweep by animateFloatAsState(
                    targetValue  = if (isCompleted) 360f else 0f,
                    animationSpec = tween(600),
                    label = "sweep"
                )
                Canvas(modifier = Modifier.size(44.dp)) {
                    drawArc(
                        color       = GlassStroke,
                        startAngle  = -90f,
                        sweepAngle  = 360f,
                        useCenter   = false,
                        style       = Stroke(3.dp.toPx(), cap = StrokeCap.Round)
                    )
                    if (sweep > 0f) {
                        drawArc(
                            brush       = Brush.sweepGradient(VioletGrad),
                            startAngle  = -90f,
                            sweepAngle  = sweep,
                            useCenter   = false,
                            style       = Stroke(3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
                if (isCompleted) {
                    Icon(Icons.Default.Check, null, tint = NeonViolet, modifier = Modifier.size(20.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .border(2.dp, TextSecondary.copy(alpha = 0.4f), CircleShape)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NEON FILTER PILLS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun NeonFilterPills(
    selected: HabitFilter,
    onChange: (HabitFilter) -> Unit,
    habits: List<HabitWithStatus>
) {
    val filters = listOf(
        HabitFilter.ALL       to "All ${habits.size}",
        HabitFilter.COMPLETED to "✓ Done ${habits.count { it.isCompletedToday }}",
        HabitFilter.PENDING   to "◷ Pending ${habits.count { !it.isCompletedToday }}",
        HabitFilter.STREAK    to "🔥 Streaks"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { (f, label) ->
            val isSel = selected == f
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(
                        if (isSel) Brush.horizontalGradient(VioletGrad)
                        else Brush.horizontalGradient(listOf(GlassWhite, GlassWhite))
                    )
                    .border(
                        1.dp,
                        if (isSel) NeonViolet.copy(alpha = 0f) else GlassStroke,
                        RoundedCornerShape(50.dp)
                    )
                    .clickable { onChange(f) }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSel) Color.White else TextSecondary
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NEON FAB
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun NeonFAB(onClick: () -> Unit) {
    val inf = rememberInfiniteTransition(label = "fab")
    val glow by inf.animateFloat(
        0.6f, 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "fab_g"
    )
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(VioletGrad))
            .shadow(elevation = 16.dp * glow, shape = CircleShape, ambientColor = NeonViolet, spotColor = NeonCyan)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add Habit", tint = Color.White, modifier = Modifier.size(28.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DARK SNACKBAR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun DarkSnackbar(data: SnackbarData) {
    Snackbar(
        snackbarData     = data,
        containerColor   = CardDark,
        contentColor     = TextPrimary,
        actionColor      = NeonViolet,
        shape            = RoundedCornerShape(16.dp),
        modifier         = Modifier.padding(16.dp)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// MOTIVATIONAL FOOTER
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun DarkMotivationalFooter(rate: Float) {
    val quote = when {
        rate >= 0.8f -> "\"Success is the sum of small efforts\nrepeated day in and day out.\" 💫"
        rate >= 0.5f -> "\"A journey of a thousand miles begins\nwith a single step.\" 🚀"
        else         -> "\"The secret of getting ahead is\ngetting started.\" ✨"
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassWhite)
            .border(1.dp, GlassStroke, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Text(
            quote,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            fontWeight = FontWeight.Medium,
            lineHeight = 22.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LOADING / ERROR / EMPTY
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun DarkLoadingView() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        CircularProgressIndicator(
            color       = NeonViolet,
            strokeWidth = 3.dp,
            modifier    = Modifier.size(48.dp)
        )
    }
}

@Composable
fun DarkErrorView(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(64.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )
    }
}
@Composable
fun DarkEmptyState(filter: HabitFilter) {

    val (emoji, title, subtitle) = when (filter) {
        HabitFilter.ALL ->
            Triple("🎯", "No habits yet", "Start building better habits today!")

        HabitFilter.COMPLETED ->
            Triple("✅", "Nothing completed yet", "Mark your first habit as done!")

        HabitFilter.PENDING ->
            Triple("🎉", "All caught up!", "Amazing work! Come back tomorrow.")

        HabitFilter.STREAK ->
            Triple("🔥", "No streaks yet", "Complete habits daily to build streaks.")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),

        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = emoji,
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )
    }
}
// ─────────────────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────────────────
fun getGreeting(): String {
    val h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        h < 12 -> "Good Morning ☀️"
        h < 17 -> "Good Afternoon 🌤️"
        else   -> "Good Evening 🌙"
    }
}

fun getMotivationalText(p: Float) = when {
    p == 1f    -> "Perfect day! All done 🎉"
    p >= 0.75f -> "Almost there! Keep it up 💪"
    p >= 0.5f  -> "Great progress today! 🚀"
    p >= 0.25f -> "Good start! Keep going 🌟"
    else       -> "Let's build some habits! ✨"
}