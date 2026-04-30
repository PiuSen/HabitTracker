package com.peu.habittracker.screen



import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.peu.habittracker.util.HabitAnalytics
import com.peu.habittracker.util.OverallStatistics
import com.peu.habittracker.util.Trend
import com.peu.habittracker.viewModel.AnalyticsUiState
import com.peu.habittracker.viewModel.AnalyticsViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Analytics & Insights",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is AnalyticsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is AnalyticsUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Overall Statistics Card
                    OverallStatsCard(stats = state.overallStats)

                    // Weekly Comparison
                    WeeklyComparisonCard(
                        thisWeek = state.overallStats.thisWeekCompletions,
                        lastWeek = state.overallStats.lastWeekCompletions
                    )

                    // Individual Habit Analytics
                    Text(
                        "Habit Performance",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    state.habitAnalytics.forEach { analytics ->
                        HabitAnalyticsCard(analytics = analytics)
                    }
                }
            }

            is AnalyticsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OverallStatsCard(stats: OverallStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Overall Progress",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBadge(
                    label = "Total Habits",
                    value = stats.totalHabits.toString(),
                    icon = "📋"
                )
                StatBadge(
                    label = "Active Streaks",
                    value = stats.currentActiveStreaks.toString(),
                    icon = "🔥"
                )
                StatBadge(
                    label = "This Month",
                    value = stats.thisMonthCompletions.toString(),
                    icon = "✅"
                )
            }

            HorizontalDivider()

            // Completion Rate Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Completion Rate",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "${(stats.overallCompletionRate * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { stats.overallCompletionRate },
                        modifier = Modifier.size(80.dp),
                        strokeWidth = 8.dp,
                        color = when {
                            stats.overallCompletionRate >= 0.8f -> Color(0xFF4CAF50)
                            stats.overallCompletionRate >= 0.5f -> Color(0xFFFFC107)
                            else -> Color(0xFFFF5722)
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun StatBadge(label: String, value: String, icon: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WeeklyComparisonCard(thisWeek: Int, lastWeek: Int) {
    val change = thisWeek - lastWeek
    val isImproving = change > 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = if (isImproving) Color(0xFF4CAF50) else Color(0xFFFF5722)
                )
                Text(
                    "Weekly Comparison",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                WeekStat("This Week", thisWeek, MaterialTheme.colorScheme.primary)
                WeekStat("Last Week", lastWeek, MaterialTheme.colorScheme.outline)
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isImproving)
                    Color(0xFF4CAF50).copy(alpha = 0.1f)
                else
                    Color(0xFFFF5722).copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isImproving) "↑" else "↓",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (isImproving) Color(0xFF4CAF50) else Color(0xFFFF5722)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${kotlin.math.abs(change)} ${if (isImproving) "more" else "fewer"} completions",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun WeekStat(label: String, value: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun HabitAnalyticsCard(analytics: HabitAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = analytics.habitIcon,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Column {
                        Text(
                            text = analytics.habitName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${analytics.completionRatePercentage}% completion rate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Trend Indicator
                TrendBadge(trend = analytics.trend)
            }

            // Progress Bar
            LinearProgressIndicator(
                progress = { analytics.completionRate },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    analytics.completionRate >= 0.8f -> Color(0xFF4CAF50)
                    analytics.completionRate >= 0.5f -> Color(0xFFFFC107)
                    else -> Color(0xFFFF5722)
                },
            )

            // Last 7 Days Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                analytics.last7DaysProgress.forEach { completed ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (completed)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (completed) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MiniStat("Streak", "${analytics.currentStreak}", "🔥")
                MiniStat("Best", "${analytics.longestStreak}", "⭐")
                MiniStat("Total", "${analytics.totalCompletions}", "✅")
            }

            // Best/Worst Days
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    DayInfo("Best Day", analytics.bestDayOfWeek, Color(0xFF4CAF50))
                    DayInfo("Worst Day", analytics.worstDayOfWeek, Color(0xFFFF5722))
                }
            }
        }
    }
}

@Composable
fun TrendBadge(trend: Trend) {
    val (icon, color, text) = when (trend) {
        Trend.IMPROVING -> Triple("📈", Color(0xFF4CAF50), "Improving")
        Trend.STABLE -> Triple("➡️", Color(0xFFFFC107), "Stable")
        Trend.DECLINING -> Triple("📉", Color(0xFFFF5722), "Declining")
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, style = MaterialTheme.typography.bodySmall)
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MiniStat(label: String, value: String, icon: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = icon)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DayInfo(label: String, day: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = day,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// ============================================================================
// ACHIEVEMENTS SCREEN
// ============================================================================



// ============================================================================
// UPDATED ADD HABIT SCREEN WITH CATEGORY SELECTOR
// ============================================================================

// Add this to your existing AddHabitScreen after the description field:

/*
var selectedCategoryId by remember { mutableStateOf<Long?>(null) }

// Category Selector Section
SectionHeader(
    title = "Choose Category",
    subtitle = "Organize your habits"
)

CategorySelector(
    selectedCategoryId = selectedCategoryId,
    onCategorySelect = { category ->
        selectedCategoryId = category?.id
        viewModel.onCategorySelect(category?.id)
    }
)
*/