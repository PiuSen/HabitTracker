package com.peu.habittracker.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.peu.habittracker.viewModel.*
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ─── Design Tokens ────────────────────────────────────────────────────────────

private val Ink900   = Color(0xFF0D0D12)
private val Ink700   = Color(0xFF1C1C27)
private val Ink500   = Color(0xFF3A3A4F)
private val Ink300   = Color(0xFF6E6E8A)
private val Ink100   = Color(0xFFBBBBCC)
private val Ink050   = Color(0xFFF0F0F6)

private val Violet   = Color(0xFF7C5CFC)
private val VioletSoft = Color(0xFFEDE8FF)
private val VioletDeep = Color(0xFF4E30D9)

private val Sage     = Color(0xFF3ECC8A)
private val SageSoft = Color(0xFFE3F9EE)
private val Amber    = Color(0xFFFFB547)
private val AmberSoft = Color(0xFFFFF3DC)
private val Coral    = Color(0xFFFF6B6B)
private val CoralSoft = Color(0xFFFFECEC)

private val SurfaceLight = Color(0xFFFAFAFC)
private val SurfaceDark  = Color(0xFF131318)

enum class HabitFilter { ALL, COMPLETED, PENDING, STREAK }

// ─── HomeScreen ───────────────────────────────────────────────────────────────

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
    val uiState      by viewModel.uiState.collectAsState()
    val deletedHabit by viewModel.deletedHabit.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedFilter by remember { mutableStateOf(HabitFilter.ALL) }
    val listState = rememberLazyListState()
    val isScrolled by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    LaunchedEffect(deletedHabit) {
        deletedHabit?.let { habit ->
            val result = snackbarHostState.showSnackbar(
                message     = "${habit.name} deleted",
                actionLabel = "Undo",
                duration    = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete()
        }
    }

    Scaffold(
        containerColor = SurfaceLight,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData       = data,
                    containerColor     = Ink700,
                    contentColor       = Color.White,
                    actionColor        = Violet,
                    shape              = RoundedCornerShape(14.dp),
                    modifier           = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick          = onNavigateToAddHabit,
                containerColor   = Violet,
                contentColor     = Color.White,
                elevation        = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                ),
                shape            = RoundedCornerShape(18.dp),
                modifier         = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, "Add", modifier = Modifier.size(24.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Sticky Header ──────────────────────────────────────────────
            HomeHeader(
                uiState             = uiState,
                isScrolled          = isScrolled,
                onStatistics        = onNavigateToStatistics,
                onSettings          = onNavigateToSettings,
                onAnalytics         = onNavigateToAnalytics,
                onAchievement       = onNavigateToAchievement
            )

            // ── Filter Pills ───────────────────────────────────────────────
            if (uiState is HomeUiState.Success) {
                val habits = (uiState as HomeUiState.Success).habits
                FilterPillRow(
                    selected   = selectedFilter,
                    onChange   = { selectedFilter = it },
                    habits     = habits
                )
            }

            // ── Content ───────────────────────────────────────────────────
            when (val state = uiState) {
                is HomeUiState.Loading -> LoadingView()
                is HomeUiState.Error   -> ErrorView(state.message)
                is HomeUiState.Success -> {
                    val filtered = when (selectedFilter) {
                        HabitFilter.ALL       -> state.habits
                        HabitFilter.COMPLETED -> state.habits.filter { it.isCompletedToday }
                        HabitFilter.PENDING   -> state.habits.filter { !it.isCompletedToday }
                        HabitFilter.STREAK    -> state.habits.sortedByDescending { it.habit.currentStreak }
                    }
                    if (filtered.isEmpty()) {
                        EmptyHabitsView(filter = selectedFilter)
                    } else {
                        LazyColumn(
                            state           = listState,
                            modifier        = Modifier.fillMaxSize(),
                            contentPadding  = PaddingValues(
                                start  = 16.dp,
                                end    = 16.dp,
                                top    = 8.dp,
                                bottom = 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Daily progress banner (top)
                            item {
                                DailyProgressBanner(habits = state.habits)
                            }
                            items(
                                items = filtered,
                                key   = { it.habit.id }
                            ) { habitWithStatus ->
                                SwipeToDeleteHabitItem(
                                    habitWithStatus = habitWithStatus,
                                    onToggle        = { viewModel.toggleHabitCompletion(it) },
                                    onClick         = { onNavigateToDetail(it) },
                                    onDelete        = { viewModel.deleteHabit(it) },
                                    modifier        = Modifier.animateItem(
                                        placementSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness    = Spring.StiffnessMedium
                                        )
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── HomeHeader ───────────────────────────────────────────────────────────────

@Composable
private fun HomeHeader(
    uiState: HomeUiState,
    isScrolled: Boolean,
    onStatistics: () -> Unit,
    onSettings: () -> Unit,
    onAnalytics: () -> Unit,
    onAchievement: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        color      = SurfaceLight,
        tonalElevation = 0.dp,
        modifier   = Modifier
            .fillMaxWidth()
            .drawBehind {
                if (isScrolled) {
                    drawLine(
                        color       = Ink050,
                        start       = Offset(0f, size.height),
                        end         = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
                Text(
                    text       = greetingText(),
                    style      = MaterialTheme.typography.labelMedium,
                    color      = Ink300,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text       = "My Habits",
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color      = Ink900
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Stats button
                IconButton(
                    onClick  = onStatistics,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Ink050)
                ) {
                    Icon(
                        Icons.Default.BarChart,
                        contentDescription = "Statistics",
                        tint     = Ink500,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // More menu
                Box {
                    IconButton(
                        onClick  = { showMenu = !showMenu },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Ink050)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint     = Ink500,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded         = showMenu,
                        onDismissRequest = { showMenu = false },
                        shape            = RoundedCornerShape(16.dp)
                    ) {
                        listOf(
                            Triple(Icons.Default.Analytics,     "Analytics",    onAnalytics),
                            Triple(Icons.Default.EmojiEvents,   "Achievements", onAchievement),
                            Triple(Icons.Default.Download,       "Export data",  {}),
                            Triple(Icons.Default.Settings,       "Settings",     onSettings)
                        ).forEach { (icon, label, action) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        label,
                                        style      = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(icon, null, tint = Violet, modifier = Modifier.size(18.dp))
                                },
                                onClick = { showMenu = false; action() }
                            )
                        }
                    }
                }
            }
        }
        }
    }


// ─── Daily Progress Banner ────────────────────────────────────────────────────

@Composable
private fun DailyProgressBanner(habits: List<HabitWithStatus>) {
    if (habits.isEmpty()) return
    val completed = habits.count { it.isCompletedToday }
    val total     = habits.size
    val progress  = completed.toFloat() / total

    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label         = "progress"
    )

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = Violet),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(Violet, VioletDeep))
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text       = "Today's progress",
                            style      = MaterialTheme.typography.labelMedium,
                            color      = Color.White.copy(alpha = 0.75f)
                        )
                        Text(
                            text       = "$completed of $total completed",
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }
                    // Circular progress mini
                    Box(
                        modifier         = Modifier.size(52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress    = { animatedProgress },
                            modifier    = Modifier.fillMaxSize(),
                            color       = Color.White,
                            trackColor  = Color.White.copy(alpha = 0.25f),
                            strokeWidth = 5.dp,
                            strokeCap   = StrokeCap.Round
                        )
                        Text(
                            text       = "${(progress * 100).toInt()}%",
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White,
                            fontSize   = 10.sp
                        )
                    }
                }

                // Linear bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White)
                    )
                }

                // Motivational text
                val message = when {
                    progress == 1f   -> "Perfect day! All done 🎉"
                    progress >= 0.7f -> "Almost there, keep going!"
                    progress >= 0.4f -> "Good momentum building"
                    total == 0       -> "No habits yet"
                    else             -> "Start your day strong"
                }
                Text(
                    text  = message,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// ─── Filter Pill Row ──────────────────────────────────────────────────────────

@Composable
private fun FilterPillRow(
    selected: HabitFilter,
    onChange: (HabitFilter) -> Unit,
    habits: List<HabitWithStatus>
) {
    val filters = listOf(
        HabitFilter.ALL       to "All (${habits.size})",
        HabitFilter.COMPLETED to "Done (${habits.count { it.isCompletedToday }})",
        HabitFilter.PENDING   to "Pending (${habits.count { !it.isCompletedToday }})",
        HabitFilter.STREAK    to "🔥 Streaks"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { (filter, label) ->
            val isSelected = selected == filter
            Surface(
                onClick   = { onChange(filter) },
                shape     = RoundedCornerShape(50.dp),
                color     = if (isSelected) Violet else Ink050,
                modifier  = Modifier.height(34.dp)
            ) {
                Row(
                    modifier          = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text       = label,
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color      = if (isSelected) Color.White else Ink500
                    )
                }
            }
        }
    }
}

// ─── Empty State ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyHabitsView(filter: HabitFilter) {
    val (emoji, title, subtitle) = when (filter) {
        HabitFilter.ALL       -> Triple("🎯", "No habits yet", "Tap + to create your first")
        HabitFilter.COMPLETED -> Triple("✅", "None completed yet", "Start checking off habits!")
        HabitFilter.PENDING   -> Triple("🎉", "All done today!", "Come back tomorrow")
        HabitFilter.STREAK    -> Triple("🔥", "No active streaks", "Complete daily to build one")
    }
    Column(
        modifier              = Modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center
    ) {
        Text(emoji, style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(20.dp))
        Text(
            title,
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color      = Ink900
        )
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Ink300
        )
    }
}

// ─── Loading / Error ──────────────────────────────────────────────────────────

@Composable
private fun LoadingView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Violet, strokeWidth = 3.dp)
    }
}

@Composable
private fun ErrorView(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier            = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint     = Coral
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = Coral
            )
        }
    }
}

// ─── Util ─────────────────────────────────────────────────────────────────────

private fun greetingText(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else      -> "Good evening"
    }
}