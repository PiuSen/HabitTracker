package com.peu.habittracker.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.peu.habittracker.ui.theme.*
import com.peu.habittracker.viewModel.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class HabitFilter { ALL, COMPLETED, PENDING, STREAK }

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    var selectedFilter by remember { mutableStateOf(HabitFilter.ALL) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Parallax scroll effect
    val scrollOffset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }
    val isScrolled by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

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

    Box(modifier = Modifier.fillMaxSize()) {
        // Animated gradient background
        AnimatedGradientBackground()

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    PremiumSnackbar(data)
                }
            },
            floatingActionButton = {
                PremiumFAB(onClick = onNavigateToAddHabit)
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Premium Header with glassmorphism
                PremiumHeader(
                    uiState = uiState,
                    isScrolled = isScrolled,
                    scrollOffset = scrollOffset,
                    onStatistics = onNavigateToStatistics,
                    onSettings = onNavigateToSettings,
                    onAnalytics = onNavigateToAnalytics,
                    onAchievement = onNavigateToAchievement
                )

                // Filter Pills
                if (uiState is HomeUiState.Success) {
                    val habits = (uiState as HomeUiState.Success).habits
                    GradientFilterPills(
                        selected = selectedFilter,
                        onChange = { selectedFilter = it },
                        habits = habits
                    )
                }

                // Main Content
                when (val state = uiState) {
                    is HomeUiState.Loading -> {
                        PremiumLoadingView()
                    }
                    is HomeUiState.Error -> {
                        PremiumErrorView(state.message)
                    }
                    is HomeUiState.Success -> {
                        val filtered = when (selectedFilter) {
                            HabitFilter.ALL -> state.habits
                            HabitFilter.COMPLETED -> state.habits.filter { it.isCompletedToday }
                            HabitFilter.PENDING -> state.habits.filter { !it.isCompletedToday }
                            HabitFilter.STREAK -> state.habits.sortedByDescending { it.habit.currentStreak }
                        }

                        if (filtered.isEmpty()) {
                            PremiumEmptyState(filter = selectedFilter)
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 8.dp,
                                    bottom = 100.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Hero Progress Card
                                item {
                                    HeroProgressCard(habits = state.habits)
                                }

                                // Quick Stats Row
                                item {
                                    QuickStatsRow(habits = state.habits)
                                }

                                // Habit Items
                                items(
                                    items = filtered,
                                    key = { it.habit.id }
                                ) { habitWithStatus ->
                                    PremiumHabitCard(
                                        habitWithStatus = habitWithStatus,
                                        // Pass the ID explicitly to the toggle/detail/delete functions
                                        onToggle = { id ->
                                            viewModel.toggleHabitCompletion(id)
                                        },

//                                                onDelete = { id ->
//                                            viewModel.deleteHabit(id)
//                                        },
                                        onClick = { id -> onNavigateToDetail(id) },
                                        //onDelete = { id -> viewModel.deleteHabit(habitWithStatus.habit) },
                                        modifier = Modifier.animateItem(
                                            placementSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    )
                                }


                                // Motivational footer
                                item {
                                    MotivationalFooter(completionRate =
                                        if (state.habits.isNotEmpty())
                                            state.habits.count { it.isCompletedToday }.toFloat() / state.habits.size
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PremiumHabitCard(
    habitWithStatus: HabitWithStatus,
    onToggle: (Long) -> Unit,
    onClick: (Long) -> Unit,
   // onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val habit = habitWithStatus.habit
    val habitId = habit.id
    val isCompleted = habitWithStatus.isCompletedToday

    val backgroundColor by animateColorAsState(
        targetValue = if (isCompleted)
            Color.White.copy(alpha = 0.9f)
        else Color.White,
        animationSpec = tween(400),
        label = "card_bg"
    )

    val scale by animateFloatAsState(
        targetValue = if (isCompleted) 0.98f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable { onClick(habitId) },   // ✅ consistent
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // LEFT SIDE
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isCompleted)
                                Brush.linearGradient(listOf(GradientPrimaryStart, GradientPrimaryEnd))
                            else
                                Brush.linearGradient(listOf(Color(0xFFF3F4F6), Color(0xFFF3F4F6)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = habit.icon.ifBlank { "✨" }
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = habit.name,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = if (habit.currentStreak > 0)
                                Color(0xFFFF8E53)
                            else Color.LightGray,
                            modifier = Modifier.size(14.dp)
                        )

                        Text("${habit.currentStreak} day streak")
                    }
                }
            }

            // RIGHT SIDE (TOGGLE)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                onToggle(habitId)   // ✅ FIXED
                            }
                        )
                    }
            ) {

                val animatedSweep by animateFloatAsState(
                    targetValue = if (isCompleted) 360f else 0f,
                    animationSpec = tween(600),
                    label = "sweep"
                )

                Canvas(modifier = Modifier.size(44.dp)) {
                    drawArc(
                        brush = Brush.linearGradient(
                            listOf(GradientPrimaryStart, GradientPrimaryEnd)
                        ),
                        startAngle = -90f,
                        sweepAngle = animatedSweep,
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx())
                    )
                }

                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = GradientPrimaryStart
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Circle,
                        contentDescription = null,
                        tint = Color.LightGray
                    )
                }
            }
        }
    }
}
// ═══ Animated Gradient Background ═══════════════════════════════════════════

@Composable
fun AnimatedGradientBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")

    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFAFAFC),
                        Color(0xFFF3F4F6).copy(alpha = 0.5f + offset * 0.5f)
                    )
                )
            )
    )
}

// ═══ Premium Header ═══════════════════════════════════════════════════════

@Composable
fun PremiumHeader(
    uiState: HomeUiState,
    isScrolled: Boolean,
    scrollOffset: Int,
    onStatistics: () -> Unit,
    onSettings: () -> Unit,
    onAnalytics: () -> Unit,
    onAchievement: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    // Glassmorphism effect
    val alpha by animateFloatAsState(
        targetValue = if (isScrolled) 0.95f else 0f,
        animationSpec = tween(300),
        label = "header_alpha"
    )

    Surface(
        color = Color.White.copy(alpha = alpha),
        tonalElevation = if (isScrolled) 2.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .blur(if (isScrolled) 0.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getGreeting(),
                    style = MaterialTheme.typography.labelLarge,
                    color = GradientPrimaryStart,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "My Habits",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Gradient Stats Button
                GradientIconButton(
                    onClick = onStatistics,
                    icon = Icons.Default.BarChart
                )

                // Menu
                Box {
                    GradientIconButton(
                        onClick = { showMenu = !showMenu },
                        icon = Icons.Default.MoreVert
                    )

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        MenuOption(Icons.Default.Analytics, "Analytics", onAnalytics) { showMenu = false }
                        MenuOption(Icons.Default.EmojiEvents, "Achievements", onAchievement) { showMenu = false }
                        MenuOption(Icons.Default.Download, "Export Data", {}) { showMenu = false }
                        MenuOption(Icons.Default.Settings, "Settings", onSettings) { showMenu = false }
                    }
                }
            }
        }
    }
}

@Composable
fun GradientIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(GradientPrimaryStart.copy(alpha = 0.1f), GradientPrimaryEnd.copy(alpha = 0.1f))
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = GradientPrimaryStart,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun MenuOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        },
        leadingIcon = {
            Icon(icon, null, tint = GradientPrimaryStart)
        },
        onClick = {
            onDismiss()
            onClick()
        }
    )
}

// ═══ Hero Progress Card ═══════════════════════════════════════════════════

@Composable
fun HeroProgressCard(habits: List<HabitWithStatus>) {
    if (habits.isEmpty()) return

    val completed = habits.count { it.isCompletedToday }
    val total = habits.size
    val progress = completed.toFloat() / total

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "progress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(GradientPrimaryStart, GradientPrimaryEnd),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
                .padding(24.dp)
        ) {
            // Decorative circles
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.1f),
                    radius = 120.dp.toPx(),
                    center = Offset(size.width * 0.8f, -50.dp.toPx())
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = 80.dp.toPx(),
                    center = Offset(-20.dp.toPx(), size.height * 0.7f)
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Today's Progress",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "$completed",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "/ $total",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                // Animated Progress Bar
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.White, Color.White.copy(alpha = 0.9f))
                                    )
                                )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            getMotivationalText(progress),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ═══ Quick Stats Row ═══════════════════════════════════════════════════════

@Composable
fun QuickStatsRow(habits: List<HabitWithStatus>) {
    val totalStreaks = habits.sumOf { it.habit.currentStreak }
    val longestStreak = habits.maxOfOrNull { it.habit.longestStreak } ?: 0
    val totalCompletions = habits.sumOf { it.habit.totalCompletions }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatCard(
            icon = "🔥",
            value = totalStreaks.toString(),
            label = "Active Streaks",
            gradient = Brush.linearGradient(listOf(Color(0xFFFF6B6B), Color(0xFFFF8E53))),
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            icon = "⭐",
            value = longestStreak.toString(),
            label = "Best Streak",
            gradient = Brush.linearGradient(listOf(Color(0xFFFFC107), Color(0xFFFF9800))),
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            icon = "✅",
            value = totalCompletions.toString(),
            label = "Completed",
            gradient = Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickStatCard(
    icon: String,
    value: String,
    label: String,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient.copy(alpha = 0.1f))
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(
                    icon,
                    style = MaterialTheme.typography.headlineMedium
                )
                Column {
                    Text(
                        value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ═══ Gradient Filter Pills ═══════════════════════════════════════════════

@Composable
fun GradientFilterPills(
    selected: HabitFilter,
    onChange: (HabitFilter) -> Unit,
    habits: List<HabitWithStatus>
) {
    val filters = listOf(
        HabitFilter.ALL to "All (${habits.size})",
        HabitFilter.COMPLETED to "Done (${habits.count { it.isCompletedToday }})",
        HabitFilter.PENDING to "Pending (${habits.count { !it.isCompletedToday }})",
        HabitFilter.STREAK to "🔥 Streaks"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        filters.forEach { (filter, label) ->
            val isSelected = selected == filter

            Surface(
                onClick = { onChange(filter) },
                shape = RoundedCornerShape(50.dp),
                color = Color.Transparent,
                modifier = Modifier.height(40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected)
                                Brush.horizontalGradient(listOf(GradientPrimaryStart, GradientPrimaryEnd))
                            else
                                Brush.horizontalGradient(listOf(Color(0xFFF3F4F6), Color(0xFFF3F4F6)))
                        )
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ═══ Premium FAB ═══════════════════════════════════════════════════════════

@Composable
fun PremiumFAB(onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }

    FloatingActionButton(
        onClick = {
            isPressed = true
            onClick()
        },
        containerColor = Color.Transparent,
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(0.dp),
        modifier = Modifier
            .size(64.dp)
            .scale(if (isPressed) 0.95f else 1f)
            .background(
                Brush.linearGradient(listOf(GradientPrimaryStart, GradientPrimaryEnd)),
                shape = RoundedCornerShape(50.dp)
            )
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = "Add Habit",
            modifier = Modifier.size(28.dp)
        )
    }
}

// ═══ Premium Snackbar ═══════════════════════════════════════════════════════

@Composable
fun PremiumSnackbar(data: SnackbarData) {
    Snackbar(
        snackbarData = data,
        containerColor = Color(0xFF1E1E2D),
        contentColor = Color.White,
        actionColor = GradientPrimaryStart,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(16.dp)
    )
}

// ═══ Helper Functions ═══════════════════════════════════════════════════════

fun getGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good Morning ☀️"
        hour < 17 -> "Good Afternoon 🌤️"
        else -> "Good Evening 🌙"
    }
}

fun getMotivationalText(progress: Float): String {
    return when {
        progress == 1f -> "Perfect day! All done 🎉"
        progress >= 0.75f -> "Almost there! Keep it up 💪"
        progress >= 0.5f -> "Great progress today! 🚀"
        progress >= 0.25f -> "Good start! Keep going 🌟"
        else -> "Let's build some habits! ✨"
    }
}

fun Brush.copy(alpha: Float): Brush {
    // Helper to apply alpha to gradient
    return this
}

// ═══ Loading & Error Views ═══════════════════════════════════════════════

@Composable
fun PremiumLoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = GradientPrimaryStart,
            strokeWidth = 4.dp,
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
fun PremiumErrorView(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PremiumEmptyState(filter: HabitFilter) {
    val (emoji, title, subtitle) = when (filter) {
        HabitFilter.ALL -> Triple("🎯", "No habits yet", "Start building better habits today!")
        HabitFilter.COMPLETED -> Triple("✅", "Nothing completed yet", "Mark your first habit as done!")
        HabitFilter.PENDING -> Triple("🎉", "All caught up!", "Amazing work! Come back tomorrow")
        HabitFilter.STREAK -> Triple("🔥", "No streaks yet", "Complete habits daily to start streaks")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(24.dp))
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MotivationalFooter(completionRate: Float) {
    val quote = when {
        completionRate >= 0.8f -> "\"Success is the sum of small efforts repeated day in and day out.\" 💫"
        completionRate >= 0.5f -> "\"A journey of a thousand miles begins with a single step.\" 🚀"
        else -> "\"The secret of getting ahead is getting started.\" ✨"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f))
    ) {
        Text(
            quote,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(20.dp)
        )
    }
}