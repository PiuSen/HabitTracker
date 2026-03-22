package com.peu.habittracker.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.peu.habittracker.viewModel.HomeViewModel
import kotlin.math.cos
import kotlin.math.sin

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Statistics & Insights",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Overview Stats
            when (uiState) {
                is com.peu.habittracker.viewModel.HomeUiState.Success -> {
                    val habits = (uiState as com.peu.habittracker.viewModel.HomeUiState.Success).habits
                    val totalHabits = habits.size
                    val completedToday = habits.count { it.isCompletedToday }
                    val totalStreaks = habits.sumOf { it.habit.currentStreak }
                    val longestStreak = habits.maxOfOrNull { it.habit.longestStreak } ?: 0

                    // Summary Cards
                    SummaryCards(
                        totalHabits = totalHabits,
                        completedToday = completedToday,
                        totalStreaks = totalStreaks,
                        longestStreak = longestStreak
                    )

                    // Completion Rate Card
                    CompletionRateCard(
                        completed = completedToday,
                        total = totalHabits
                    )

                    // Weekly Progress Chart
                    WeeklyProgressChart(habits = habits)

                    // Top Performing Habits
                    TopHabitsCard(habits = habits)

                    // Motivational Quote
                    MotivationalCard(completionRate = if (totalHabits > 0) completedToday.toFloat() / totalHabits else 0f)
                }
                else -> {
                    Card {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCards(
    totalHabits: Int,
    completedToday: Int,
    totalStreaks: Int,
    longestStreak: Int
) {
    Text(
        "Overview",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Total Habits",
            value = totalHabits.toString(),
            icon = Icons.Default.List,
            color = Color(0xFF6200EE),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Completed",
            value = completedToday.toString(),
            icon = Icons.Default.CheckCircle,
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Total Streaks",
            value = totalStreaks.toString(),
            icon = Icons.Default.LocalFireDepartment,
            color = Color(0xFFFF5722),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Best Streak",
            value = longestStreak.toString(),
            icon = Icons.Default.Star,
            color = Color(0xFFFFC107),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CompletionRateCard(
    completed: Int,
    total: Int
) {
    val percentage = if (total > 0) (completed.toFloat() / total * 100).toInt() else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Today's Progress",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$completed of $total completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = percentage / 100f,
                        modifier = Modifier.size(80.dp),
                        strokeWidth = 8.dp,
                        color = when {
                            percentage >= 80 -> Color(0xFF4CAF50)
                            percentage >= 50 -> Color(0xFFFFC107)
                            else -> Color(0xFFFF5722)
                        }
                    )
                    Text(
                        "$percentage%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyProgressChart(
    habits: List<com.peu.habittracker.viewModel.HabitWithStatus>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Weekly Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Bar Chart (Simulated data for demo)
            val weekData = listOf(85, 90, 75, 95, 88, 92, 80) // Mock completion percentages
            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                weekData.forEachIndexed { index, value ->
                    BarChartItem(
                        value = value,
                        label = days[index],
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun BarChartItem(
    value: Int,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height((value * 1.5f).dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(color, color.copy(alpha = 0.6f))
                    ),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TopHabitsCard(
    habits: List<com.peu.habittracker.viewModel.HabitWithStatus>
) {
    val topHabits = habits.sortedByDescending { it.habit.currentStreak }.take(3)

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
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFFC107)
                )
                Text(
                    "Top Performing Habits",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (topHabits.isEmpty()) {
                Text(
                    "No habits yet. Start tracking!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                topHabits.forEachIndexed { index, habitWithStatus ->
                    TopHabitItem(
                        rank = index + 1,
                        habit = habitWithStatus.habit,
                        color = Color(habitWithStatus.habit.color)
                    )
                    if (index < topHabits.size - 1) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun TopHabitItem(
    rank: Int,
    habit: com.peu.habittracker.db.Habit,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank Badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = when (rank) {
                        1 -> Color(0xFFFFD700)
                        2 -> Color(0xFFC0C0C0)
                        3 -> Color(0xFFCD7F32)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Habit Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = habit.icon, style = MaterialTheme.typography.titleLarge)
        }

        // Habit Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${habit.currentStreak} day streak",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Streak Icon
        Text(
            text = "🔥",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun MotivationalCard(completionRate: Float) {
    val (emoji, message) = when {
        completionRate >= 0.8f -> "🎉" to "Outstanding! You're crushing it today!"
        completionRate >= 0.5f -> "💪" to "Great progress! Keep up the momentum!"
        completionRate > 0f -> "🌱" to "Good start! Every step counts!"
        else -> "🎯" to "Ready to build some habits today?"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.displaySmall
            )
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
