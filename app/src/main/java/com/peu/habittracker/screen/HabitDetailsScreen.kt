package com.peu.habittracker.screen


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.peu.habittracker.viewModel.HabitDetailViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    viewModel: HabitDetailViewModel = hiltViewModel()
) {
    val habit by viewModel.habit.collectAsState(initial = null)
    val completions by viewModel.completions.collectAsState(initial = emptyList())
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habit Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            )
        }
    ) { padding ->
        habit?.let { h ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(h.color).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(h.color)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = h.icon,
                                style = MaterialTheme.typography.displaySmall
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = h.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (h.description.isNotBlank()) {
                                Text(
                                    text = h.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Current Streak",
                        value = "${h.currentStreak}",
                        icon = "🔥",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Best Streak",
                        value = "${h.longestStreak}",
                        icon = "⭐",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Days",
                        value = "${h.totalCompletions}",
                        icon = "✅",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Completion Rate
                val completionRate = if (completions.isNotEmpty()) {
                    (completions.count { it.completed }.toFloat() / completions.size * 100).toInt()
                } else 0

                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Completion Rate",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "$completionRate%",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = completionRate / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }

                // Calendar View (Last 7 weeks)
                CalendarView(
                    completions = completions,
                    habitColor = Color(h.color)
                )

                // Recent Activity
                Text(
                    "Recent Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                val recentCompletions = completions
                    .sortedByDescending { it.date }
                    .take(7)

                if (recentCompletions.isEmpty()) {
                    Card {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No activity yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    recentCompletions.forEach { completion ->
                        ActivityItem(completion = completion)
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Habit?") },
            text = { Text("This will permanently delete this habit and all its data.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // TODO: Implement delete
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarView(
    completions: List<com.peu.habittracker.db.HabitCompletion>,
    habitColor: Color
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Last 7 Weeks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Day headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Calendar grid (7 weeks x 7 days)
            val today = LocalDate.now()
            val startDate = today.minusWeeks(6).with(java.time.DayOfWeek.MONDAY)

            val completionDates = completions
                .filter { it.completed }
                .map { it.date }
                .toSet()

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(252.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(49) { index ->
                    val date = startDate.plusDays(index.toLong())
                    val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val isCompleted = completionDates.contains(dateStr)
                    val isToday = date == today
                    val isFuture = date > today

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    isFuture -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    isCompleted -> habitColor.copy(alpha = 0.8f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                            .then(
                                if (isToday) Modifier.border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(6.dp)
                                ) else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
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
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActivityItem(
    completion: com.peu.habittracker.db.HabitCompletion
) {
    val date = LocalDate.parse(completion.date)
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (completion.completed) Icons.Default.CheckCircle
                    else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (completion.completed)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outline
                )

                Column {
                    Text(
                        date.format(formatter),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    if (completion.note.isNotBlank()) {
                        Text(
                            completion.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                if (completion.completed) "Completed" else "Skipped",
                style = MaterialTheme.typography.labelMedium,
                color = if (completion.completed)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
